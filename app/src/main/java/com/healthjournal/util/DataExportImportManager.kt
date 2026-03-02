package com.healthjournal.util

import android.content.Context
import android.net.Uri
import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.*
import com.healthjournal.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupData(
    val meta: ExportMeta = ExportMeta(),
    val symptoms: List<SymptomDto> = emptyList(),
    val vitalSigns: List<VitalSignDto> = emptyList(),
    val medications: List<MedicationDto> = emptyList(),
    val medicationLogs: List<MedicationLogDto> = emptyList(),
    val aiReports: List<AiReportDto> = emptyList(),
    val familyMembers: List<FamilyMemberDto> = emptyList(),
    val diseases: List<DiseaseDto> = emptyList(),
    val userSettings: UserSettingsBackup? = null,
    val predefinedData: PredefinedDataBackup? = null
)

@Serializable
data class UserSettingsBackup(
    val userName: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val aiConsentGiven: Boolean = false,
    val languageMode: String = "SYSTEM",
    val weight: String = "",
    val height: String = "",
    val knownDiseases: List<String> = emptyList()
)

@Serializable
data class PredefinedDataBackup(
    val disabledSymptoms: List<String> = emptyList(),
    val disabledMedications: List<String> = emptyList(),
    val disabledRelations: List<String> = emptyList(),
    val customSymptoms: List<String> = emptyList(),
    val customMedications: List<String> = emptyList(),
    val customRelations: List<String> = emptyList(),
    val enabledSymptomKeys: List<String> = emptyList(),
    val enabledMedicationKeys: List<String> = emptyList(),
    val enabledRelationKeys: List<String> = emptyList()
)

class DataExportImportManager(
    private val context: Context,
    private val symptomStore: JsonFileStore<SymptomDto>,
    private val vitalSignStore: JsonFileStore<VitalSignDto>,
    private val medicationStore: JsonFileStore<MedicationDto>,
    private val medicationLogStore: JsonFileStore<MedicationLogDto>,
    private val aiReportStore: JsonFileStore<AiReportDto>,
    private val familyMemberStore: JsonFileStore<FamilyMemberDto>,
    private val diseaseStore: JsonFileStore<DiseaseDto>,
    private val userSettingsRepository: UserSettingsRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private suspend fun buildBackupData(): BackupData {
        val settings = userSettingsRepository.getUserSettings().first()

        val predefinedPrefs = context.predefinedDataStore.data.first()
        val disabledSymptoms = predefinedPrefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet()
        val disabledMedications = predefinedPrefs[PredefinedDataKeys.DISABLED_MEDICATIONS] ?: emptySet()
        val disabledRelations = predefinedPrefs[PredefinedDataKeys.DISABLED_RELATIONS] ?: emptySet()
        val customSymptoms = predefinedPrefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet()
        val customMedications = predefinedPrefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] ?: emptySet()
        val customRelations = predefinedPrefs[PredefinedDataKeys.CUSTOM_RELATIONS] ?: emptySet()

        val predefinedData = PredefinedDataBackup(
            disabledSymptoms = disabledSymptoms.toList(),
            disabledMedications = disabledMedications.toList(),
            disabledRelations = disabledRelations.toList(),
            customSymptoms = customSymptoms.toList(),
            customMedications = customMedications.toList(),
            customRelations = customRelations.toList(),
            enabledSymptomKeys = PredefinedData.symptoms
                .filter { it.key !in disabledSymptoms }
                .map { it.key } + customSymptoms.toList(),
            enabledMedicationKeys = PredefinedData.medications
                .filter { it.key !in disabledMedications }
                .map { it.key } + customMedications.toList(),
            enabledRelationKeys = PredefinedData.relationshipItems
                .filter { it.key !in disabledRelations }
                .map { it.key } + customRelations.toList()
        )

        return BackupData(
            meta = ExportMeta(uiLanguage = settings.languageMode),
            symptoms = symptomStore.getAll(),
            vitalSigns = vitalSignStore.getAll(),
            medications = medicationStore.getAll(),
            medicationLogs = medicationLogStore.getAll(),
            aiReports = aiReportStore.getAll(),
            familyMembers = familyMemberStore.getAll(),
            diseases = diseaseStore.getAll(),
            userSettings = UserSettingsBackup(
                userName = settings.userName,
                doctorName = settings.doctorName,
                doctorPhone = settings.doctorPhone,
                aiConsentGiven = settings.aiConsentGiven,
                languageMode = settings.languageMode,
                weight = settings.weight,
                height = settings.height,
                knownDiseases = settings.knownDiseases
            ),
            predefinedData = predefinedData
        )
    }

    suspend fun exportToString(): String {
        val backup = buildBackupData()
        return json.encodeToString(BackupData.serializer(), backup)
    }

    suspend fun exportData(uri: Uri) {
        val jsonString = exportToString()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Cannot open output stream")
    }

    suspend fun importFromString(jsonString: String) {
        val backup = json.decodeFromString(BackupData.serializer(), jsonString)
        restoreBackup(backup)
    }

    suspend fun importData(uri: Uri) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("Cannot open input stream")

        importFromString(jsonString)
    }

    private suspend fun restoreBackup(backup: BackupData) {
        // Replace all data stores
        symptomStore.replaceAll(backup.symptoms)
        vitalSignStore.replaceAll(backup.vitalSigns)
        medicationStore.replaceAll(backup.medications)
        medicationLogStore.replaceAll(backup.medicationLogs)
        aiReportStore.replaceAll(backup.aiReports)
        familyMemberStore.replaceAll(backup.familyMembers)
        diseaseStore.replaceAll(backup.diseases)

        // Restore user settings (keep onboarding completed, don't change active profile)
        backup.userSettings?.let { settingsBackup ->
            val current = userSettingsRepository.getUserSettings().first()
            userSettingsRepository.updateUserSettings(
                current.copy(
                    userName = settingsBackup.userName,
                    doctorName = settingsBackup.doctorName,
                    doctorPhone = settingsBackup.doctorPhone,
                    aiConsentGiven = settingsBackup.aiConsentGiven,
                    weight = settingsBackup.weight,
                    height = settingsBackup.height,
                    knownDiseases = settingsBackup.knownDiseases
                )
            )
        }

        // Restore predefined data settings
        backup.predefinedData?.let { pd ->
            context.predefinedDataStore.updateData { prefs ->
                val mutable = prefs.toMutablePreferences()
                mutable[PredefinedDataKeys.DISABLED_SYMPTOMS] = pd.disabledSymptoms.toSet()
                mutable[PredefinedDataKeys.DISABLED_MEDICATIONS] = pd.disabledMedications.toSet()
                mutable[PredefinedDataKeys.DISABLED_RELATIONS] = pd.disabledRelations.toSet()
                mutable[PredefinedDataKeys.CUSTOM_SYMPTOMS] = pd.customSymptoms.toSet()
                mutable[PredefinedDataKeys.CUSTOM_MEDICATIONS] = pd.customMedications.toSet()
                mutable[PredefinedDataKeys.CUSTOM_RELATIONS] = pd.customRelations.toSet()
                mutable
            }
        }
    }
}
