package com.healthjournal.domain.usecase

import com.healthjournal.domain.ai.AiService
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.ReportType
import com.healthjournal.domain.model.ai.AiInput
import com.healthjournal.domain.model.ai.AiSettings
import com.healthjournal.domain.repository.AiReportRepository
import com.healthjournal.domain.repository.MedicationRepository
import com.healthjournal.domain.repository.SymptomRepository
import com.healthjournal.domain.repository.VitalSignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class GenerateAiSummaryUseCase(
    private val aiService: AiService,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository,
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(
        periodDays: Int = 7,
        outputLanguage: String = "ru",
        aiSettings: AiSettings = AiSettings()
    ): Result<AiReport> = runCatching {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()
        val medications = medicationRepository.getActiveMedications().first()

        val input = AiInput(
            symptoms = symptoms,
            vitals = vitals,
            medications = medications,
            periodDays = periodDays,
            outputLanguage = outputLanguage
        )

        val result = aiService.generateDoctorSummary(input, aiSettings)
        val report = AiReport(
            content = result.text,
            type = ReportType.SUMMARY,
            periodDays = periodDays,
            generatedAt = LocalDateTime.now()
        )
        val id = reportRepository.insertReport(report)
        report.copy(id = id)
    }
}

class GeneratePatternAnalysisUseCase(
    private val aiService: AiService,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository
) {
    suspend operator fun invoke(
        periodDays: Int = 30,
        outputLanguage: String = "ru",
        aiSettings: AiSettings = AiSettings()
    ): Result<AiReport> = runCatching {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()

        val input = AiInput(
            symptoms = symptoms,
            vitals = vitals,
            medications = emptyList(),
            periodDays = periodDays,
            outputLanguage = outputLanguage
        )

        val result = aiService.analyzePatterns(input, aiSettings)
        val report = AiReport(
            content = result.text,
            type = ReportType.PATTERN_ANALYSIS,
            periodDays = periodDays,
            generatedAt = LocalDateTime.now()
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
