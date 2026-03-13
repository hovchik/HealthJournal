package com.healthjournal.domain.usecase

import com.healthjournal.domain.ai.AiService
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.ReportType
import com.healthjournal.domain.model.ai.AiInput
import com.healthjournal.domain.model.ai.AiSettings
import com.healthjournal.domain.repository.AiReportRepository
import com.healthjournal.domain.repository.DiseaseRepository
import com.healthjournal.domain.repository.FamilyMemberRepository
import com.healthjournal.domain.repository.MedicationRepository
import com.healthjournal.domain.repository.SymptomRepository
import com.healthjournal.domain.repository.UserSettingsRepository
import com.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class GenerateAiSummaryUseCase(
    private val aiService: AiService,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository,
    private val medicationRepository: MedicationRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val diseaseRepository: DiseaseRepository
) {
    suspend operator fun invoke(
        periodDays: Int = 2,
        outputLanguage: String = "ru",
        aiSettings: AiSettings = AiSettings(),
        profileId: Long = 0,
        diseaseId: Long = 0
    ): Result<AiReport> = runCatching {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val userSettings = userSettingsRepository.getUserSettings().first()
        var symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
            .filter { it.profileId == profileId }
        var vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()
            .filter { it.profileId == profileId }
        var medications = medicationRepository.getActiveMedications().first()
            .filter { it.profileId == profileId }

        if (diseaseId != 0L) {
            symptoms = symptoms.filter { it.diseaseId == diseaseId }
            vitals = vitals.filter { it.diseaseId == diseaseId }
            medications = medications.filter { it.diseaseId == diseaseId }
        }

        val diseaseName = if (diseaseId != 0L) {
            diseaseRepository.getDiseaseById(diseaseId)?.name ?: ""
        } else ""

        val (patientWeight, patientHeight, patientAge, patientGender, patientDiseases) = if (profileId != 0L) {
            val member = familyMemberRepository.getMemberById(profileId)
            PatientInfo(
                member?.weight ?: "", member?.height ?: "",
                member?.age ?: "", member?.gender ?: "",
                member?.knownDiseases ?: emptyList()
            )
        } else {
            PatientInfo(
                userSettings.weight, userSettings.height,
                userSettings.age, userSettings.gender,
                userSettings.knownDiseases
            )
        }

        val input = AiInput(
            symptoms = symptoms,
            vitals = vitals,
            medications = medications,
            periodDays = periodDays,
            outputLanguage = outputLanguage,
            knownDiseases = patientDiseases,
            weight = patientWeight,
            height = patientHeight,
            age = patientAge,
            gender = patientGender,
            diseaseName = diseaseName
        )

        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val result = aiService.generateDoctorSummary(input, aiSettings)
        val providerName = result.modelUsed.ifBlank { result.providerId.key }
        val report = AiReport(
            content = result.text,
            type = ReportType.SUMMARY,
            periodDays = periodDays,
            generatedAt = LocalDateTime.now(),
            profileId = profileId,
            diseaseId = diseaseId,
            providerName = providerName,
            modelUsed = result.modelUsed,
            prompt = prompt.user
        )
        val id = reportRepository.insertReport(report)
        report.copy(id = id)
    }
}

class GeneratePatternAnalysisUseCase(
    private val aiService: AiService,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val familyMemberRepository: FamilyMemberRepository,
    private val diseaseRepository: DiseaseRepository
) {
    suspend operator fun invoke(
        periodDays: Int = 4,
        outputLanguage: String = "ru",
        aiSettings: AiSettings = AiSettings(),
        profileId: Long = 0,
        diseaseId: Long = 0
    ): Result<AiReport> = runCatching {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val userSettings = userSettingsRepository.getUserSettings().first()
        var symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
            .filter { it.profileId == profileId }
        var vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()
            .filter { it.profileId == profileId }

        if (diseaseId != 0L) {
            symptoms = symptoms.filter { it.diseaseId == diseaseId }
            vitals = vitals.filter { it.diseaseId == diseaseId }
        }

        val diseaseName = if (diseaseId != 0L) {
            diseaseRepository.getDiseaseById(diseaseId)?.name ?: ""
        } else ""

        val (patientWeight, patientHeight, patientAge, patientGender, patientDiseases) = if (profileId != 0L) {
            val member = familyMemberRepository.getMemberById(profileId)
            PatientInfo(
                member?.weight ?: "", member?.height ?: "",
                member?.age ?: "", member?.gender ?: "",
                member?.knownDiseases ?: emptyList()
            )
        } else {
            PatientInfo(
                userSettings.weight, userSettings.height,
                userSettings.age, userSettings.gender,
                userSettings.knownDiseases
            )
        }

        val input = AiInput(
            symptoms = symptoms,
            vitals = vitals,
            medications = emptyList(),
            periodDays = periodDays,
            outputLanguage = outputLanguage,
            knownDiseases = patientDiseases,
            weight = patientWeight,
            height = patientHeight,
            age = patientAge,
            gender = patientGender,
            diseaseName = diseaseName
        )

        val prompt = PromptTemplate.buildPatternPrompt(input)
        val result = aiService.analyzePatterns(input, aiSettings)
        val providerName = result.modelUsed.ifBlank { result.providerId.key }
        val report = AiReport(
            content = result.text,
            type = ReportType.PATTERN_ANALYSIS,
            periodDays = periodDays,
            generatedAt = LocalDateTime.now(),
            profileId = profileId,
            diseaseId = diseaseId,
            providerName = providerName,
            modelUsed = result.modelUsed,
            prompt = prompt.user
        )
        val id = reportRepository.insertReport(report)
        report.copy(id = id)
    }
}

class GetAllReportsUseCase(
    private val repository: AiReportRepository
) {
    operator fun invoke(): Flow<List<AiReport>> = repository.getAllReports()
}

private data class PatientInfo(
    val weight: String,
    val height: String,
    val age: String,
    val gender: String,
    val knownDiseases: List<String>
)
