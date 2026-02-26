package com.healthjournal.domain.usecase

import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.ReportType
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.repository.AiProvider
import com.healthjournal.domain.repository.AiReportRepository
import com.healthjournal.domain.repository.SymptomRepository
import com.healthjournal.domain.repository.VitalSignRepository
import com.healthjournal.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class GenerateAiSummaryUseCase @Inject constructor(
    private val aiProvider: AiProvider,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository,
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(periodDays: Int = 7): Result<AiReport> {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()
        val medications = medicationRepository.getActiveMedications().first()

        val prompt = buildSummaryPrompt(symptoms, vitals, medications, periodDays)

        return aiProvider.generateSummary(prompt).map { content ->
            val report = AiReport(
                content = content,
                type = ReportType.SUMMARY,
                periodDays = periodDays,
                generatedAt = LocalDateTime.now()
            )
            val id = reportRepository.insertReport(report)
            report.copy(id = id)
        }
    }

    private fun buildSummaryPrompt(
        symptoms: List<Symptom>,
        vitals: List<VitalSign>,
        medications: List<Medication>,
        periodDays: Int
    ): String = buildString {
        appendLine("Составь структурированное медицинское резюме на русском языке для врача за последние $periodDays дней.")
        appendLine("ВАЖНО: Ты НЕ ставишь диагнозы и НЕ назначаешь лекарства. Используй формулировки «возможные причины», «рекомендуется обсудить с врачом».")
        appendLine()
        appendLine("=== СИМПТОМЫ ===")
        if (symptoms.isEmpty()) {
            appendLine("Нет записей о симптомах за период.")
        } else {
            symptoms.forEach { s ->
                appendLine("- ${s.name}: интенсивность ${s.intensity}/10, ${s.recordedAt.toLocalDate()}")
                if (s.triggers.isNotEmpty()) appendLine("  Триггеры: ${s.triggers.joinToString(", ")}")
                if (s.notes.isNotBlank()) appendLine("  Заметки: ${s.notes}")
            }
        }
        appendLine()
        appendLine("=== ПОКАЗАТЕЛИ ===")
        if (vitals.isEmpty()) {
            appendLine("Нет записей о показателях за период.")
        } else {
            vitals.forEach { v ->
                val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
                appendLine("- ${v.type.displayName}: $valueStr ${v.type.unit}, ${v.recordedAt.toLocalDate()}")
            }
        }
        appendLine()
        appendLine("=== ЛЕКАРСТВА ===")
        if (medications.isEmpty()) {
            appendLine("Нет активных лекарств.")
        } else {
            medications.forEach { m ->
                appendLine("- ${m.name} ${m.dosage}, ${m.frequency}")
            }
        }
    }
}

class GeneratePatternAnalysisUseCase @Inject constructor(
    private val aiProvider: AiProvider,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository
) {
    suspend operator fun invoke(periodDays: Int = 30): Result<AiReport> {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()

        val prompt = buildPatternPrompt(symptoms, vitals, periodDays)

        return aiProvider.analyzePatterns(prompt).map { content ->
            val report = AiReport(
                content = content,
                type = ReportType.PATTERN_ANALYSIS,
                periodDays = periodDays,
                generatedAt = LocalDateTime.now()
            )
            val id = reportRepository.insertReport(report)
            report.copy(id = id)
        }
    }

    private fun buildPatternPrompt(
        symptoms: List<Symptom>,
        vitals: List<VitalSign>,
        periodDays: Int
    ): String = buildString {
        appendLine("Проанализируй корреляции и паттерны в медицинских данных за $periodDays дней.")
        appendLine("ВАЖНО: Ты НЕ ставишь диагнозы. Используй формулировки «возможная связь», «стоит обратить внимание».")
        appendLine()
        appendLine("=== СИМПТОМЫ ===")
        symptoms.forEach { s ->
            appendLine("- ${s.recordedAt}: ${s.name} (${s.intensity}/10)")
        }
        appendLine()
        appendLine("=== ПОКАЗАТЕЛИ ===")
        vitals.forEach { v ->
            val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
            appendLine("- ${v.recordedAt}: ${v.type.displayName} = $valueStr ${v.type.unit}")
        }
        appendLine()
        appendLine("Найди закономерности, повторяющиеся паттерны, возможные корреляции между симптомами и показателями.")
    }
}

class GetAllReportsUseCase @Inject constructor(
    private val repository: AiReportRepository
) {
    operator fun invoke(): Flow<List<AiReport>> = repository.getAllReports()
}
