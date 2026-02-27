package com.healthjournal.domain.ai

import com.healthjournal.domain.model.ai.AiInput

/**
 * Shared prompt builder. Provider-agnostic: builds system + user prompt pairs.
 * Providers then adapt these to their own API format (Claude, OpenAI, local).
 */
object PromptTemplate {

    data class Prompt(
        val system: String,
        val user: String
    )

    fun buildSummaryPrompt(input: AiInput): Prompt {
        val lang = input.outputLanguage
        val t = SummaryL10n.forLanguage(lang)

        val system = buildString {
            appendLine(t.systemRole)
            appendLine(t.disclaimer)
        }

        val user = buildString {
            appendLine(t.mainInstruction(input.periodDays))
            appendLine()
            appendLine("=== ${t.symptomsHeader} ===")
            if (input.symptoms.isEmpty()) {
                appendLine(t.noSymptoms)
            } else {
                input.symptoms.forEach { s ->
                    appendLine("- ${s.name}: ${t.intensity} ${s.intensity}/10, ${s.recordedAt.toLocalDate()}")
                    if (s.triggers.isNotEmpty()) appendLine("  ${t.triggers}: ${s.triggers.joinToString(", ")}")
                    if (s.notes.isNotBlank()) appendLine("  ${t.notes}: ${s.notes}")
                }
            }
            appendLine()
            appendLine("=== ${t.vitalsHeader} ===")
            if (input.vitals.isEmpty()) {
                appendLine(t.noVitals)
            } else {
                input.vitals.forEach { v ->
                    val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
                    appendLine("- ${v.type.displayName}: $valueStr ${v.type.unit}, ${v.recordedAt.toLocalDate()}")
                }
            }
            appendLine()
            appendLine("=== ${t.medicationsHeader} ===")
            if (input.medications.isEmpty()) {
                appendLine(t.noMedications)
            } else {
                input.medications.forEach { m ->
                    appendLine("- ${m.name} ${m.dosage}, ${m.frequency}")
                }
            }
        }
        return Prompt(system, user)
    }

    fun buildPatternPrompt(input: AiInput): Prompt {
        val lang = input.outputLanguage
        val t = PatternL10n.forLanguage(lang)

        val system = buildString {
            appendLine(t.systemRole)
            appendLine(t.disclaimer)
        }

        val user = buildString {
            appendLine(t.mainInstruction(input.periodDays))
            appendLine()
            appendLine("=== ${t.symptomsHeader} ===")
            input.symptoms.forEach { s ->
                appendLine("- ${s.recordedAt}: ${s.name} (${s.intensity}/10)")
            }
            appendLine()
            appendLine("=== ${t.vitalsHeader} ===")
            input.vitals.forEach { v ->
                val valueStr = if (v.secondaryValue != null) "${v.value.toInt()}/${v.secondaryValue.toInt()}" else "${v.value}"
                appendLine("- ${v.recordedAt}: ${v.type.displayName} = $valueStr ${v.type.unit}")
            }
            appendLine()
            appendLine(t.findPatterns)
        }
        return Prompt(system, user)
    }
}

// --- Localized prompt fragments ---

private data class SummaryTexts(
    val systemRole: String,
    val disclaimer: String,
    val mainInstruction: (Int) -> String,
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

private object SummaryL10n {
    fun forLanguage(lang: String): SummaryTexts = when (lang) {
        "en" -> SummaryTexts(
            systemRole = "You are a medical data assistant. You help summarize health journal data for doctors.",
            disclaimer = "IMPORTANT: You do NOT diagnose conditions and do NOT prescribe medications. Use phrases like \"possible causes\", \"recommend discussing with your doctor\".",
            mainInstruction = { d -> "Create a structured medical summary for a doctor for the last $d days. Respond in English." },
            symptomsHeader = "SYMPTOMS", vitalsHeader = "VITALS", medicationsHeader = "MEDICATIONS",
            noSymptoms = "No symptom records for this period.", noVitals = "No vital signs recorded for this period.", noMedications = "No active medications.",
            intensity = "intensity", triggers = "Triggers", notes = "Notes"
        )
        "es" -> SummaryTexts(
            systemRole = "Eres un asistente de datos medicos. Ayudas a resumir datos del diario de salud para medicos.",
            disclaimer = "IMPORTANTE: NO diagnosticas enfermedades y NO recetas medicamentos. Usa frases como <<posibles causas>>, <<se recomienda consultar con su medico>>.",
            mainInstruction = { d -> "Crea un resumen medico estructurado para un medico de los ultimos $d dias. Responde en espanol." },
            symptomsHeader = "SINTOMAS", vitalsHeader = "SIGNOS VITALES", medicationsHeader = "MEDICAMENTOS",
            noSymptoms = "No hay registros de sintomas para este periodo.", noVitals = "No hay registros de signos vitales para este periodo.", noMedications = "No hay medicamentos activos.",
            intensity = "intensidad", triggers = "Desencadenantes", notes = "Notas"
        )
        "zh-CN" -> SummaryTexts(
            systemRole = "你是一名医疗数据助手。你帮助为医生总结健康日记数据。",
            disclaimer = "重要提示：你不做诊断，也不开处方。请使用「可能的原因」、「建议与医生讨论」等措辞。",
            mainInstruction = { d -> "为医生创建过去${d}天的结构化医疗摘要。请用简体中文回复。" },
            symptomsHeader = "症状", vitalsHeader = "生命体征", medicationsHeader = "药物",
            noSymptoms = "该期间无症状记录。", noVitals = "该期间无生命体征记录。", noMedications = "无正在使用的药物。",
            intensity = "强度", triggers = "诱因", notes = "备注"
        )
        else -> SummaryTexts(
            systemRole = "Ты медицинский ассистент. Ты помогаешь составлять резюме данных дневника здоровья для врача.",
            disclaimer = "ВАЖНО: Ты НЕ ставишь диагнозы и НЕ назначаешь лекарства. Используй формулировки <<возможные причины>>, <<рекомендуется обсудить с врачом>>.",
            mainInstruction = { d -> "Составь структурированное медицинское резюме для врача за последние $d дней. Отвечай на русском языке." },
            symptomsHeader = "СИМПТОМЫ", vitalsHeader = "ПОКАЗАТЕЛИ", medicationsHeader = "ЛЕКАРСТВА",
            noSymptoms = "Нет записей о симптомах за период.", noVitals = "Нет записей о показателях за период.", noMedications = "Нет активных лекарств.",
            intensity = "интенсивность", triggers = "Триггеры", notes = "Заметки"
        )
    }
}

private data class PatternTexts(
    val systemRole: String,
    val disclaimer: String,
    val mainInstruction: (Int) -> String,
    val symptomsHeader: String,
    val vitalsHeader: String,
    val findPatterns: String
)

private object PatternL10n {
    fun forLanguage(lang: String): PatternTexts = when (lang) {
        "en" -> PatternTexts(
            systemRole = "You are a medical data analyst. You find patterns and correlations in health data.",
            disclaimer = "IMPORTANT: You do NOT diagnose. Use phrases like \"possible correlation\", \"worth noting\".",
            mainInstruction = { d -> "Analyze correlations and patterns in the medical data for $d days. Respond in English." },
            symptomsHeader = "SYMPTOMS", vitalsHeader = "VITALS",
            findPatterns = "Find regularities, recurring patterns, and possible correlations between symptoms and vitals."
        )
        "es" -> PatternTexts(
            systemRole = "Eres un analista de datos medicos. Encuentras patrones y correlaciones en datos de salud.",
            disclaimer = "IMPORTANTE: NO diagnosticas. Usa frases como <<posible correlacion>>, <<vale la pena observar>>.",
            mainInstruction = { d -> "Analiza las correlaciones y patrones en los datos medicos de $d dias. Responde en espanol." },
            symptomsHeader = "SINTOMAS", vitalsHeader = "SIGNOS VITALES",
            findPatterns = "Encuentra regularidades, patrones recurrentes y posibles correlaciones entre sintomas y signos vitales."
        )
        "zh-CN" -> PatternTexts(
            systemRole = "你是一名医疗数据分析师。你在健康数据中寻找模式和相关性。",
            disclaimer = "重要提示：你不做诊断。请使用「可能的关联」、「值得注意」等措辞。",
            mainInstruction = { d -> "分析${d}天内医疗数据中的相关性和模式。请用简体中文回复。" },
            symptomsHeader = "症状", vitalsHeader = "生命体征",
            findPatterns = "寻找规律、重复出现的模式以及症状和生命体征之间的可能关联。"
        )
        else -> PatternTexts(
            systemRole = "Ты медицинский аналитик данных. Ты находишь паттерны и корреляции в данных о здоровье.",
            disclaimer = "ВАЖНО: Ты НЕ ставишь диагнозы. Используй формулировки <<возможная связь>>, <<стоит обратить внимание>>.",
            mainInstruction = { d -> "Проанализируй корреляции и паттерны в медицинских данных за $d дней. Отвечай на русском языке." },
            symptomsHeader = "СИМПТОМЫ", vitalsHeader = "ПОКАЗАТЕЛИ",
            findPatterns = "Найди закономерности, повторяющиеся паттерны, возможные корреляции между симптомами и показателями."
        )
    }
}
