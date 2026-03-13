package com.healthjournal.di

import android.content.Context
import com.healthjournal.BuildConfig
import com.healthjournal.data.ai.AiPreferences
import com.healthjournal.data.ai.ClaudeAiProviderImpl
import com.healthjournal.data.ai.DeviceAiCapabilityDetector
import com.healthjournal.data.ai.LocalAiBenchmarkRunner
import com.healthjournal.data.ai.LocalModelManager
import com.healthjournal.data.ai.ModelCompatibilityValidator
import com.healthjournal.data.ai.ModelInstaller
import com.healthjournal.data.ai.OpenAiCompatibleProviderImpl
import com.healthjournal.data.ai.provider.AiProviderSelector
import com.healthjournal.data.ai.provider.CustomLocalModelProvider
import com.healthjournal.data.ai.provider.SystemAiProvider
import com.healthjournal.data.ai.runtime.LiteRtRuntimeAdapter
import com.healthjournal.data.ai.runtime.MediaPipeLlmRuntimeAdapter
import com.healthjournal.data.ai.runtime.SystemAiRuntimeAdapter
import com.healthjournal.data.local.db.AppDatabase
import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.*
import com.healthjournal.data.remote.api.ClaudeApi
import com.healthjournal.data.remote.openai.OpenAiApi
import com.healthjournal.data.repository.*
import com.healthjournal.domain.ai.AiProviderRegistry
import com.healthjournal.domain.ai.AiService
import com.healthjournal.domain.model.ai.ClaudeConfig
import com.healthjournal.domain.model.ai.OpenAiConfig
import com.healthjournal.domain.repository.*
import com.healthjournal.domain.usecase.*
import com.healthjournal.util.DataExportImportManager
import com.healthjournal.util.PrivacyRedactor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    // JSON
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    // Stores
    private val symptomStore = JsonFileStore(
        context = context, fileName = "symptoms.json",
        serializer = SymptomDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val vitalSignStore = JsonFileStore(
        context = context, fileName = "vital_signs.json",
        serializer = VitalSignDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val medicationStore = JsonFileStore(
        context = context, fileName = "medications.json",
        serializer = MedicationDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val medicationLogStore = JsonFileStore(
        context = context, fileName = "medication_logs.json",
        serializer = MedicationLogDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val aiReportStore = JsonFileStore(
        context = context, fileName = "ai_reports.json",
        serializer = AiReportDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val familyMemberStore = JsonFileStore(
        context = context, fileName = "family_members.json",
        serializer = FamilyMemberDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )
    private val diseaseStore = JsonFileStore(
        context = context, fileName = "diseases.json",
        serializer = DiseaseDto.serializer(), json = json,
        getId = { it.id }, setId = { item, id -> item.copy(id = id) }
    )

    // Network helpers
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun buildClaudeApi(config: ClaudeConfig): ClaudeApi {
        val apiKey = config.apiKey.ifBlank { BuildConfig.CLAUDE_API_KEY }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ClaudeApi::class.java)
    }

    private fun buildOpenAiApi(config: OpenAiConfig): OpenAiApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${config.apiKey}")
                    .addHeader("Content-Type", "application/json")
                config.extraHeaders.forEach { (key, value) ->
                    builder.addHeader(key, value)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiApi::class.java)
    }

    // === On-device AI infrastructure ===

    // Database
    private val appDatabase = AppDatabase.getInstance(context)
    private val localAiModelDao = appDatabase.localAiModelDao()

    // Preferences & detection
    val aiPreferences = AiPreferences(context)
    val deviceCapabilityDetector = DeviceAiCapabilityDetector(context)

    // Model management
    val modelCompatibilityValidator = ModelCompatibilityValidator(deviceCapabilityDetector)
    val localModelManager = LocalModelManager(localAiModelDao, aiPreferences)
    val modelInstaller = ModelInstaller(context, localModelManager, modelCompatibilityValidator)

    // Runtime adapters
    val mediaPipeRuntime = MediaPipeLlmRuntimeAdapter(context)
    val liteRtRuntime = LiteRtRuntimeAdapter(context)
    val systemAiRuntime = SystemAiRuntimeAdapter(context)

    // Benchmark
    val benchmarkRunner = LocalAiBenchmarkRunner(context)

    // AI providers
    private val claudeProvider = ClaudeAiProviderImpl(::buildClaudeApi)
    private val openAiProvider = OpenAiCompatibleProviderImpl(::buildOpenAiApi)
    private val customLocalProvider = CustomLocalModelProvider(
        localModelManager, mediaPipeRuntime, liteRtRuntime
    )
    private val systemAiProvider = SystemAiProvider(systemAiRuntime)

    val aiProviderRegistry = AiProviderRegistry(
        listOf(claudeProvider, openAiProvider, customLocalProvider)
    )

    val aiProviderSelector = AiProviderSelector(
        aiPreferences, systemAiProvider, customLocalProvider,
        listOf(claudeProvider, openAiProvider), systemAiRuntime
    )

    private val privacyRedactor = PrivacyRedactor()
    val aiService = AiService(aiProviderRegistry, privacyRedactor, aiPreferences)

    // Repositories
    val symptomRepository: SymptomRepository = SymptomRepositoryImpl(symptomStore)
    val vitalSignRepository: VitalSignRepository = VitalSignRepositoryImpl(vitalSignStore)
    val medicationRepository: MedicationRepository = MedicationRepositoryImpl(medicationStore, medicationLogStore)
    val aiReportRepository: AiReportRepository = AiReportRepositoryImpl(aiReportStore)
    val userSettingsRepository: UserSettingsRepository = UserSettingsRepositoryImpl(context)
    val familyMemberRepository: FamilyMemberRepository = FamilyMemberRepositoryImpl(familyMemberStore)
    val diseaseRepository: DiseaseRepository = DiseaseRepositoryImpl(diseaseStore)

    // Use Cases
    val getAllSymptoms = GetAllSymptomsUseCase(symptomRepository)
    val getSymptomsByDateRange = GetSymptomsByDateRangeUseCase(symptomRepository)
    val addSymptom = AddSymptomUseCase(symptomRepository)
    val updateSymptom = UpdateSymptomUseCase(symptomRepository)
    val getSymptomById = GetSymptomByIdUseCase(symptomRepository)
    val deleteSymptom = DeleteSymptomUseCase(symptomRepository)

    val getAllVitalSigns = GetAllVitalSignsUseCase(vitalSignRepository)
    val getVitalSignsByType = GetVitalSignsByTypeUseCase(vitalSignRepository)
    val getVitalSignsByDateRange = GetVitalSignsByDateRangeUseCase(vitalSignRepository)
    val addVitalSign = AddVitalSignUseCase(vitalSignRepository)
    val updateVitalSign = UpdateVitalSignUseCase(vitalSignRepository)
    val getVitalSignById = GetVitalSignByIdUseCase(vitalSignRepository)
    val deleteVitalSign = DeleteVitalSignUseCase(vitalSignRepository)

    val getAllMedications = GetAllMedicationsUseCase(medicationRepository)
    val getActiveMedications = GetActiveMedicationsUseCase(medicationRepository)
    val addMedication = AddMedicationUseCase(medicationRepository)
    val updateMedication = UpdateMedicationUseCase(medicationRepository)
    val getMedicationById = GetMedicationByIdUseCase(medicationRepository)
    val deleteMedication = DeleteMedicationUseCase(medicationRepository)
    val logMedicationTaken = LogMedicationTakenUseCase(medicationRepository)
    val getMedicationLogs = GetMedicationLogsUseCase(medicationRepository)

    val getAllDiseases = GetAllDiseasesUseCase(diseaseRepository)
    val getDiseasesByProfileId = GetDiseasesByProfileIdUseCase(diseaseRepository)
    val getDiseaseById = GetDiseaseByIdUseCase(diseaseRepository)
    val addDisease = AddDiseaseUseCase(diseaseRepository)
    val updateDisease = UpdateDiseaseUseCase(diseaseRepository)
    val deleteDisease = DeleteDiseaseUseCase(diseaseRepository)
    val softDeleteDisease = SoftDeleteDiseaseUseCase(diseaseRepository, symptomRepository, vitalSignRepository, medicationRepository)
    val restoreDisease = RestoreDiseaseUseCase(diseaseRepository, symptomRepository, vitalSignRepository, medicationRepository)
    val getDeletedDiseases = GetDeletedDiseasesUseCase(diseaseRepository)
    val getDeletedDiseasesByProfileId = GetDeletedDiseasesByProfileIdUseCase(diseaseRepository)
    val permanentlyDeleteDisease = PermanentlyDeleteDiseaseUseCase(diseaseRepository)

    val generateAiSummary = GenerateAiSummaryUseCase(aiService, aiReportRepository, symptomRepository, vitalSignRepository, medicationRepository, userSettingsRepository, familyMemberRepository, diseaseRepository)
    val generatePatternAnalysis = GeneratePatternAnalysisUseCase(aiService, aiReportRepository, symptomRepository, vitalSignRepository, userSettingsRepository, familyMemberRepository, diseaseRepository)
    val generateDiseaseAnalysis = GenerateDiseaseAnalysisUseCase(aiService, aiReportRepository, symptomRepository, vitalSignRepository, medicationRepository, userSettingsRepository, familyMemberRepository, diseaseRepository)
    val getAllReports = GetAllReportsUseCase(aiReportRepository)

    // Export/Import
    val dataExportImportManager = DataExportImportManager(
        context = context,
        symptomStore = symptomStore,
        vitalSignStore = vitalSignStore,
        medicationStore = medicationStore,
        medicationLogStore = medicationLogStore,
        aiReportStore = aiReportStore,
        familyMemberStore = familyMemberStore,
        diseaseStore = diseaseStore,
        userSettingsRepository = userSettingsRepository
    )
}
