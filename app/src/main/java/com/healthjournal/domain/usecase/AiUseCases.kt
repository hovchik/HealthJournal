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

class GenerateAiSummaryUseCase constructor(
    private val aiProvider: AiProvider,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository,
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(periodDays: Int = 7, outputLanguage: String = "ru"): Result<AiReport> {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()
        val medications = medicationRepository.getActiveMedications().first()

        val prompt = buildSummaryPrompt(symptoms, vitals, medications, periodDays, outputLanguage)

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
        periodDays: Int,
        outputLanguage: String
    ): String = buildString {
        val instructions = SummaryPromptTemplates.forLanguage(outputLanguage)
        appendLine(instructions.mainInstruction(periodDays))
        appendLine(instructions.disclaimer)
        appendLine()
        appendLine("=== ${instructions.symptomsHeader} ===")
        if (symptoms.isEmpty()) {
            appendLine(instructions.noSymptoms)
        } else {
            symptoms.forEach { s ->
                appendLine("- ${s.name}: ${instructions.intensity} ${s.intensity}/10, ${s.recordedAt.toLocalDate()}")
                if (s.triggers.isNotEmpty()) appendLine("  ${instructions.triggers}: ${s.triggers.joinToString(", ")}")
                if (s.notes.isNotBlank()) appendLine("  ${instructions.notes}: ${s.notes}")
            }
        }
        appendLine()
        appendLine("=== ${instructions.vitalsHeader} ===")
        if (vitals.isEmpty()) {
            appendLine(instructions.noVitals)
        } else {
            vitals.forEach { v ->
                val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
                appendLine("- ${v.type.displayName}: $valueStr ${v.type.unit}, ${v.recordedAt.toLocalDate()}")
            }
        }
        appendLine()
        appendLine("=== ${instructions.medicationsHeader} ===")
        if (medications.isEmpty()) {
            appendLine(instructions.noMedications)
        } else {
            medications.forEach { m ->
                appendLine("- ${m.name} ${m.dosage}, ${m.frequency}")
            }
        }
    }
}

class GeneratePatternAnalysisUseCase constructor(
    private val aiProvider: AiProvider,
    private val reportRepository: AiReportRepository,
    private val symptomRepository: SymptomRepository,
    private val vitalSignRepository: VitalSignRepository
) {
    suspend operator fun invoke(periodDays: Int = 30, outputLanguage: String = "ru"): Result<AiReport> {
        val now = LocalDateTime.now()
        val from = now.minusDays(periodDays.toLong())

        val symptoms = symptomRepository.getSymptomsByDateRange(from, now).first()
        val vitals = vitalSignRepository.getVitalSignsByDateRange(from, now).first()

        val prompt = buildPatternPrompt(symptoms, vitals, periodDays, outputLanguage)

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
        periodDays: Int,
        outputLanguage: String
    ): String = buildString {
        val instructions = PatternPromptTemplates.forLanguage(outputLanguage)
        appendLine(instructions.mainInstruction(periodDays))
        appendLine(instructions.disclaimer)
        appendLine()
        appendLine("=== ${instructions.symptomsHeader} ===")
        symptoms.forEach { s ->
            appendLine("- ${s.recordedAt}: ${s.name} (${s.intensity}/10)")
        }
        appendLine()
        appendLine("=== ${instructions.vitalsHeader} ===")
        vitals.forEach { v ->
            val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
            appendLine("- ${v.recordedAt}: ${v.type.displayName} = $valueStr ${v.type.unit}")
        }
        appendLine()
        appendLine(instructions.findPatterns)
    }
}

class GetAllReportsUseCase constructor(
    private val repository: AiReportRepository
) {
    operator fun invoke(): Flow<List<AiReport>> = repository.getAllReports()
}

// --- Prompt template data classes ---

private data class SummaryInstructions(
    val mainInstruction: (Int) -> String,
    val disclaimer: String,
    val symptomsHeader: String,
    val vitalsHeader: String,
    val medicationsHeader: String,
    val noSymptoms: String,
    val noVitals: String,
    val noMedications: String,
    val intensity: String,
    val triggers: String,
    val notes: String
)

private object SummaryPromptTemplates {
    fun forLanguage(lang: String): SummaryInstructions = when (lang) {
        "en" -> SummaryInstructions(
            mainInstruction = { days -> "Create a structured medical summary for a doctor for the last $days days. Respond in English." },
            disclaimer = "IMPORTANT: You do NOT diagnose conditions and do NOT prescribe medications. Use phrases like \"possible causes\", \"recommend discussing with your doctor\".",
            symptomsHeader = "SYMPTOMS",
            vitalsHeader = "VITALS",
            medicationsHeader = "MEDICATIONS",
            noSymptoms = "No symptom records for this period.",
            noVitals = "No vital signs recorded for this period.",
            noMedications = "No active medications.",
            intensity = "intensity",
            triggers = "Triggers",
            notes = "Notes"
        )
        "es" -> SummaryInstructions(
            mainInstruction = { days -> "Crea un resumen médico estructurado para un médico de los últimos $days días. Responde en español." },
            disclaimer = "IMPORTANTE: NO diagnosticas enfermedades y NO recetas medicamentos. Usa frases como «posibles causas», «se recomienda consultar con su médico».",
            symptomsHeader = "SÍNTOMAS",
            vitalsHeader = "SIGNOS VITALES",
            medicationsHeader = "MEDICAMENTOS",
            noSymptoms = "No hay registros de síntomas para este período.",
            noVitals = "No hay registros de signos vitales para este período.",
            noMedications = "No hay medicamentos activos.",
            intensity = "intensidad",
            triggers = "Desencadenantes",
            notes = "Notas"
        )
        "zh-CN" -> SummaryInstructions(
            mainInstruction = { days -> "为医生创建过去${days}天的结构化医疗摘要。请用简体中文回复。" },
            disclaimer = "重要提示：你不做诊断，也不开处方。请使用「可能的原因」、「建议与医生讨论」等措辞。",
            symptomsHeader = "症状",
            vitalsHeader = "生命体征",
            medicationsHeader = "药物",
            noSymptoms = "该期间无症状记录。",
            noVitals = "该期间无生命体征记录。",
            noMedications = "无正在使用的药物。",
            intensity = "强度",
            triggers = "诱因",
            notes = "备注"
        )
        else -> SummaryInstructions(
            mainInstruction = { days -> "Составь структурированное медицинское резюме для врача за последние $days дней. Отвечай на русском языке." },
            disclaimer = "ВАЖНО: Ты НЕ ставишь диагнозы и НЕ назначаешь лекарства. Используй формулировки «возможные причины», «рекомендуется обсудить с врачом».",
            symptomsHeader = "СИМПТОМЫ",
            vitalsHeader = "ПОКАЗАТЕЛИ",
            medicationsHeader = "ЛЕКАРСТВА",
            noSymptoms = "Нет записей о симптомах за период.",
            noVitals = "Нет записей о показателях за период.",
            noMedications = "Нет активных лекарств.",
            intensity = "интенсивность",
            triggers = "Триггеры",
            notes = "Заметки"
        )
    }
}

private data class PatternInstructions(
    val mainInstruction: (Int) -> String,
    val disclaimer: String,
    val symptomsHeader: String,
    val vitalsHeader: String,
    val findPatterns: String
)

private object PatternPromptTemplates {
    fun forLanguage(lang: String): PatternInstructions = when (lang) {
        "en" -> PatternInstructions(
            mainInstruction = { days -> "Analyze correlations and patterns in the medical data for $days days. Respond in English." },
            disclaimer = "IMPORTANT: You do NOT diagnose. Use phrases like \"possible correlation\", \"worth noting\".",
            symptomsHeader = "SYMPTOMS",
            vitalsHeader = "VITALS",
            findPatterns = "Find regularities, recurring patterns, and possible correlations between symptoms and vitals."
        )
        "es" -> PatternInstructions(
            mainInstruction = { days -> "Analiza las correlaciones y patrones en los datos médicos de $days días. Responde en español." },
            disclaimer = "IMPORTANTE: NO diagnosticas. Usa frases como «posible correlación», «vale la pena observar».",
            symptomsHeader = "SÍNTOMAS",
            vitalsHeader = "SIGNOS VITALES",
            findPatterns = "Encuentra regularidades, patrones recurrentes y posibles correlaciones entre síntomas y signos vitales."
        )
        "zh-CN" -> PatternInstructions(
            mainInstruction = { days -> "分析${days}天内医疗数据中的相关性和模式。请用简体中文回复。" },
            disclaimer = "重要提示：你不做诊断。请使用「可能的关联」、「值得注意」等措辞。",
            symptomsHeader = "症状",
            vitalsHeader = "生命体征",
            findPatterns = "寻找规律、重复出现的模式以及症状和生命体征之间的可能关联。"
        )
        else -> PatternInstructions(
            mainInstruction = { days -> "Проанализируй корреляции и паттерны в медицинских данных за $days дней. Отвечай на русском языке." },
            disclaimer = "ВАЖНО: Ты НЕ ставишь диагнозы. Используй формулировки «возможная связь», «стоит обратить внимание».",
            symptomsHeader = "СИМПТОМЫ",
            vitalsHeader = "ПОКАЗАТЕЛИ",
            findPatterns = "Найди закономерности, повторяющиеся паттерны, возможные корреляции между симптомами и показателями."
        )
    }
}
