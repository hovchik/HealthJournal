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

    private fun buildPatientContext(input: AiInput, t: PatientContextTexts): String = buildString {
        val hasWeight = input.weight.isNotBlank()
        val hasHeight = input.height.isNotBlank()
        val hasDiseases = input.knownDiseases.isNotEmpty()
        if (!hasWeight && !hasHeight && !hasDiseases) return@buildString

        appendLine("=== ${t.patientHeader} ===")
        if (hasWeight || hasHeight) {
            val parts = mutableListOf<String>()
            if (hasWeight) parts.add("${t.weight}: ${input.weight}")
            if (hasHeight) parts.add("${t.height}: ${input.height}")
            appendLine(parts.joinToString(", "))
        }
        if (hasDiseases) {
            appendLine("${t.knownDiseasesHeader}: ${input.knownDiseases.joinToString(", ")}")
        }
        appendLine()
    }

    fun buildSummaryPrompt(input: AiInput): Prompt {
        val lang = input.outputLanguage
        val t = SummaryL10n.forLanguage(lang)
        val pc = PatientContextL10n.forLanguage(lang)

        val system = buildString {
            appendLine(t.systemRole)
            appendLine(t.disclaimer)
        }

        val user = buildString {
            appendLine(t.mainInstruction(input.periodDays))
            appendLine()
            append(buildPatientContext(input, pc))
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
        val pc = PatientContextL10n.forLanguage(lang)

        val system = buildString {
            appendLine(t.systemRole)
            appendLine(t.disclaimer)
        }

        val user = buildString {
            appendLine(t.mainInstruction(input.periodDays))
            appendLine()
            append(buildPatientContext(input, pc))
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

// --- Patient context localization ---

private data class PatientContextTexts(
    val patientHeader: String,
    val weight: String,
    val height: String,
    val knownDiseasesHeader: String
)

private object PatientContextL10n {
    fun forLanguage(lang: String): PatientContextTexts = when (lang) {
        "en" -> PatientContextTexts(
            patientHeader = "PATIENT INFO",
            weight = "Weight",
            height = "Height",
            knownDiseasesHeader = "Known conditions"
        )
        "es" -> PatientContextTexts(
            patientHeader = "INFO DEL PACIENTE",
            weight = "Peso",
            height = "Altura",
            knownDiseasesHeader = "Condiciones conocidas"
        )
        "zh-CN" -> PatientContextTexts(
            patientHeader = "\u60A3\u8005\u4FE1\u606F",
            weight = "\u4F53\u91CD",
            height = "\u8EAB\u9AD8",
            knownDiseasesHeader = "\u5DF2\u77E5\u75BE\u75C5"
        )
        else -> PatientContextTexts(
            patientHeader = "\u0418\u041D\u0424\u041E \u041E \u041F\u0410\u0426\u0418\u0415\u041D\u0422\u0415",
            weight = "\u0412\u0435\u0441",
            height = "\u0420\u043E\u0441\u0442",
            knownDiseasesHeader = "\u0418\u0437\u0432\u0435\u0441\u0442\u043D\u044B\u0435 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F"
        )
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
            systemRole = "\u4F60\u662F\u4E00\u540D\u533B\u7597\u6570\u636E\u52A9\u624B\u3002\u4F60\u5E2E\u52A9\u4E3A\u533B\u751F\u603B\u7ED3\u5065\u5EB7\u65E5\u8BB0\u6570\u636E\u3002",
            disclaimer = "\u91CD\u8981\u63D0\u793A\uFF1A\u4F60\u4E0D\u505A\u8BCA\u65AD\uFF0C\u4E5F\u4E0D\u5F00\u5904\u65B9\u3002\u8BF7\u4F7F\u7528\u300C\u53EF\u80FD\u7684\u539F\u56E0\u300D\u3001\u300C\u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u300D\u7B49\u63AA\u8F9E\u3002",
            mainInstruction = { d -> "\u4E3A\u533B\u751F\u521B\u5EFA\u8FC7\u53BB${d}\u5929\u7684\u7ED3\u6784\u5316\u533B\u7597\u6458\u8981\u3002\u8BF7\u7528\u7B80\u4F53\u4E2D\u6587\u56DE\u590D\u3002" },
            symptomsHeader = "\u75C7\u72B6", vitalsHeader = "\u751F\u547D\u4F53\u5F81", medicationsHeader = "\u836F\u7269",
            noSymptoms = "\u8BE5\u671F\u95F4\u65E0\u75C7\u72B6\u8BB0\u5F55\u3002", noVitals = "\u8BE5\u671F\u95F4\u65E0\u751F\u547D\u4F53\u5F81\u8BB0\u5F55\u3002", noMedications = "\u65E0\u6B63\u5728\u4F7F\u7528\u7684\u836F\u7269\u3002",
            intensity = "\u5F3A\u5EA6", triggers = "\u8BF1\u56E0", notes = "\u5907\u6CE8"
        )
        else -> SummaryTexts(
            systemRole = "\u0422\u044B \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u0438\u0439 \u0430\u0441\u0441\u0438\u0441\u0442\u0435\u043D\u0442. \u0422\u044B \u043F\u043E\u043C\u043E\u0433\u0430\u0435\u0448\u044C \u0441\u043E\u0441\u0442\u0430\u0432\u043B\u044F\u0442\u044C \u0440\u0435\u0437\u044E\u043C\u0435 \u0434\u0430\u043D\u043D\u044B\u0445 \u0434\u043D\u0435\u0432\u043D\u0438\u043A\u0430 \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u044F \u0434\u043B\u044F \u0432\u0440\u0430\u0447\u0430.",
            disclaimer = "\u0412\u0410\u0416\u041D\u041E: \u0422\u044B \u041D\u0415 \u0441\u0442\u0430\u0432\u0438\u0448\u044C \u0434\u0438\u0430\u0433\u043D\u043E\u0437\u044B \u0438 \u041D\u0415 \u043D\u0430\u0437\u043D\u0430\u0447\u0430\u0435\u0448\u044C \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432\u0430. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0439 \u0444\u043E\u0440\u043C\u0443\u043B\u0438\u0440\u043E\u0432\u043A\u0438 <<\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u044B\u0435 \u043F\u0440\u0438\u0447\u0438\u043D\u044B>>, <<\u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C>>.",
            mainInstruction = { d -> "\u0421\u043E\u0441\u0442\u0430\u0432\u044C \u0441\u0442\u0440\u0443\u043A\u0442\u0443\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u043E\u0435 \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u043E\u0435 \u0440\u0435\u0437\u044E\u043C\u0435 \u0434\u043B\u044F \u0432\u0440\u0430\u0447\u0430 \u0437\u0430 \u043F\u043E\u0441\u043B\u0435\u0434\u043D\u0438\u0435 $d \u0434\u043D\u0435\u0439. \u041E\u0442\u0432\u0435\u0447\u0430\u0439 \u043D\u0430 \u0440\u0443\u0441\u0441\u043A\u043E\u043C \u044F\u0437\u044B\u043A\u0435." },
            symptomsHeader = "\u0421\u0418\u041C\u041F\u0422\u041E\u041C\u042B", vitalsHeader = "\u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0418", medicationsHeader = "\u041B\u0415\u041A\u0410\u0420\u0421\u0422\u0412\u0410",
            noSymptoms = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u0430\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.", noVitals = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u044F\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.", noMedications = "\u041D\u0435\u0442 \u0430\u043A\u0442\u0438\u0432\u043D\u044B\u0445 \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432.",
            intensity = "\u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C", triggers = "\u0422\u0440\u0438\u0433\u0433\u0435\u0440\u044B", notes = "\u0417\u0430\u043C\u0435\u0442\u043A\u0438"
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
            systemRole = "\u4F60\u662F\u4E00\u540D\u533B\u7597\u6570\u636E\u5206\u6790\u5E08\u3002\u4F60\u5728\u5065\u5EB7\u6570\u636E\u4E2D\u5BFB\u627E\u6A21\u5F0F\u548C\u76F8\u5173\u6027\u3002",
            disclaimer = "\u91CD\u8981\u63D0\u793A\uFF1A\u4F60\u4E0D\u505A\u8BCA\u65AD\u3002\u8BF7\u4F7F\u7528\u300C\u53EF\u80FD\u7684\u5173\u8054\u300D\u3001\u300C\u503C\u5F97\u6CE8\u610F\u300D\u7B49\u63AA\u8F9E\u3002",
            mainInstruction = { d -> "\u5206\u6790${d}\u5929\u5185\u533B\u7597\u6570\u636E\u4E2D\u7684\u76F8\u5173\u6027\u548C\u6A21\u5F0F\u3002\u8BF7\u7528\u7B80\u4F53\u4E2D\u6587\u56DE\u590D\u3002" },
            symptomsHeader = "\u75C7\u72B6", vitalsHeader = "\u751F\u547D\u4F53\u5F81",
            findPatterns = "\u5BFB\u627E\u89C4\u5F8B\u3001\u91CD\u590D\u51FA\u73B0\u7684\u6A21\u5F0F\u4EE5\u53CA\u75C7\u72B6\u548C\u751F\u547D\u4F53\u5F81\u4E4B\u95F4\u7684\u53EF\u80FD\u5173\u8054\u3002"
        )
        else -> PatternTexts(
            systemRole = "\u0422\u044B \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u0438\u0439 \u0430\u043D\u0430\u043B\u0438\u0442\u0438\u043A \u0434\u0430\u043D\u043D\u044B\u0445. \u0422\u044B \u043D\u0430\u0445\u043E\u0434\u0438\u0448\u044C \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u044B \u0438 \u043A\u043E\u0440\u0440\u0435\u043B\u044F\u0446\u0438\u0438 \u0432 \u0434\u0430\u043D\u043D\u044B\u0445 \u043E \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u0435.",
            disclaimer = "\u0412\u0410\u0416\u041D\u041E: \u0422\u044B \u041D\u0415 \u0441\u0442\u0430\u0432\u0438\u0448\u044C \u0434\u0438\u0430\u0433\u043D\u043E\u0437\u044B. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0439 \u0444\u043E\u0440\u043C\u0443\u043B\u0438\u0440\u043E\u0432\u043A\u0438 <<\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u0430\u044F \u0441\u0432\u044F\u0437\u044C>>, <<\u0441\u0442\u043E\u0438\u0442 \u043E\u0431\u0440\u0430\u0442\u0438\u0442\u044C \u0432\u043D\u0438\u043C\u0430\u043D\u0438\u0435>>.",
            mainInstruction = { d -> "\u041F\u0440\u043E\u0430\u043D\u0430\u043B\u0438\u0437\u0438\u0440\u0443\u0439 \u043A\u043E\u0440\u0440\u0435\u043B\u044F\u0446\u0438\u0438 \u0438 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u044B \u0432 \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u0438\u0445 \u0434\u0430\u043D\u043D\u044B\u0445 \u0437\u0430 $d \u0434\u043D\u0435\u0439. \u041E\u0442\u0432\u0435\u0447\u0430\u0439 \u043D\u0430 \u0440\u0443\u0441\u0441\u043A\u043E\u043C \u044F\u0437\u044B\u043A\u0435." },
            symptomsHeader = "\u0421\u0418\u041C\u041F\u0422\u041E\u041C\u042B", vitalsHeader = "\u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0418",
            findPatterns = "\u041D\u0430\u0439\u0434\u0438 \u0437\u0430\u043A\u043E\u043D\u043E\u043C\u0435\u0440\u043D\u043E\u0441\u0442\u0438, \u043F\u043E\u0432\u0442\u043E\u0440\u044F\u044E\u0449\u0438\u0435\u0441\u044F \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u044B, \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u044B\u0435 \u043A\u043E\u0440\u0440\u0435\u043B\u044F\u0446\u0438\u0438 \u043C\u0435\u0436\u0434\u0443 \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u0430\u043C\u0438 \u0438 \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u044F\u043C\u0438."
        )
    }
}
