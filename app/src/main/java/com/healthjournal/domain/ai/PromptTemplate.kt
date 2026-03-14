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
        val hasAge = input.age.isNotBlank()
        val hasGender = input.gender.isNotBlank()
        val hasDiseases = input.knownDiseases.isNotEmpty()
        val hasDiseaseName = input.diseaseName.isNotBlank()
        if (!hasWeight && !hasHeight && !hasAge && !hasGender && !hasDiseases && !hasDiseaseName) return@buildString

        appendLine("=== ${t.patientHeader} ===")
        if (hasAge || hasGender) {
            val parts = mutableListOf<String>()
            if (hasAge) parts.add("${t.age}: ${input.age}")
            if (hasGender) parts.add("${t.gender}: ${input.gender}")
            appendLine(parts.joinToString(", "))
        }
        if (hasWeight || hasHeight) {
            val parts = mutableListOf<String>()
            if (hasWeight) parts.add("${t.weight}: ${input.weight}")
            if (hasHeight) parts.add("${t.height}: ${input.height}")
            appendLine(parts.joinToString(", "))
        }
        if (hasDiseases) {
            appendLine("${t.knownDiseasesHeader}: ${input.knownDiseases.joinToString(", ")}")
        }
        if (hasDiseaseName) {
            appendLine("${t.analyzingDiseaseHeader}: ${input.diseaseName}")
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

    fun buildDiseaseAnalysisPrompt(input: AiInput): Prompt {
        val lang = input.outputLanguage
        val t = DiseaseAnalysisL10n.forLanguage(lang)
        val pc = PatientContextL10n.forLanguage(lang)

        val system = buildString {
            appendLine(t.systemRole)
            appendLine(t.disclaimer)
        }

        val user = buildString {
            appendLine(t.mainInstruction(input.diseaseName))
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
            appendLine()
            appendLine(t.analysisRequest)
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
    val age: String,
    val gender: String,
    val weight: String,
    val height: String,
    val knownDiseasesHeader: String,
    val analyzingDiseaseHeader: String
)

private object PatientContextL10n {
    fun forLanguage(lang: String): PatientContextTexts = when (lang) {
        "en" -> PatientContextTexts(
            patientHeader = "PATIENT INFO",
            age = "Age",
            gender = "Gender",
            weight = "Weight",
            height = "Height",
            knownDiseasesHeader = "Known conditions",
            analyzingDiseaseHeader = "Analyzing disease"
        )
        "es" -> PatientContextTexts(
            patientHeader = "INFO DEL PACIENTE",
            age = "Edad",
            gender = "G\u00E9nero",
            weight = "Peso",
            height = "Altura",
            knownDiseasesHeader = "Condiciones conocidas",
            analyzingDiseaseHeader = "Enfermedad analizada"
        )
        "zh-CN" -> PatientContextTexts(
            patientHeader = "\u60A3\u8005\u4FE1\u606F",
            age = "\u5E74\u9F84",
            gender = "\u6027\u522B",
            weight = "\u4F53\u91CD",
            height = "\u8EAB\u9AD8",
            knownDiseasesHeader = "\u5DF2\u77E5\u75BE\u75C5",
            analyzingDiseaseHeader = "\u5206\u6790\u75BE\u75C5"
        )
        "hy" -> PatientContextTexts(
            patientHeader = "ՏԵՂԵdelaysu054Fu054Eu0548u0552u0539u0546 u0540u053Bu054Eu0531u0546u0534u053B u0544u0531u054Du053Bu0546",
            age = "u054Fu0561u0580u056Bu0584",
            gender = "u054Du0565u057C",
            weight = "u0554u0561u0577",
            height = "u0540u0561u057Du0561u056F",
            knownDiseasesHeader = "u0540u0561u0575u057Fu0576u056B u0570u056Bu057Eu0561u0576u0564u0578u0582u0569u0575u0578u0582u0576u0576u0565u0580",
            analyzingDiseaseHeader = "u054Eu0565u0580u056Cu0578u0582u056Eu057Eu0578u0572 u0570u056Bu057Eu0561u0576u0564u0578u0582u0569u0575u0578u0582u0576"
        )
        else -> PatientContextTexts(
            patientHeader = "\u0418\u041D\u0424\u041E \u041E \u041F\u0410\u0426\u0418\u0415\u041D\u0422\u0415",
            age = "\u0412\u043E\u0437\u0440\u0430\u0441\u0442",
            gender = "\u041F\u043E\u043B",
            weight = "\u0412\u0435\u0441",
            height = "\u0420\u043E\u0441\u0442",
            knownDiseasesHeader = "\u0418\u0437\u0432\u0435\u0441\u0442\u043D\u044B\u0435 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F",
            analyzingDiseaseHeader = "\u0410\u043D\u0430\u043B\u0438\u0437\u0438\u0440\u0443\u0435\u043C\u043E\u0435 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u0435"
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
        "hy" -> SummaryTexts(
            systemRole = "Դուք բժշկական տվյալների օգնական ես։ Դուք օգնուм ես աмողժաмանի օրագրի տվյալները աмաջաмանական աмողժաмանի համար։",
            disclaimer = "ԿԱՌԵՎՈՌ՝ Դուք ՉԵՔ ախտորоոշուм տաмիս և ՉԵՔ դեղեր նշանակուм։ Օգտագоրծիր ՜հնարավоր պատծառներ՝՝, ՜խоրհուրդակցվուм է բժշկի հետ քննարկել՝՝։",
            mainInstruction = { d -> "Կազмեք վերժին $d օրվա կառուցվածքային բժշկական աмաջաмանական աмողժաмան։ Պատասխանեք հայերեն։" },
            symptomsHeader = "ԱԽՏԱՆԻՇՆԵՌ", vitalsHeader = "ԿԵՆՍԱԿԱՆ ՑՈՒԻՉՆԵՌ", medicationsHeader = "ԴԵՂԵՌ",
            noSymptoms = "Այս ժաмանակահատվածուм ախտանիշների գրանցուмներ չկան։", noVitals = "Այս ժաмանակահատվածուм կենսական ցուցիչների գրանցուмներ չկան։", noMedications = "Ակտիվ դեղեր չկան։",
            intensity = "ինտենսիվություն", triggers = "Պատճառներ", notes = "Նոտաներ"
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
        "hy" -> PatternTexts(
            systemRole = "Դուք բժշկական տվյալների վերլուծաբան ես։ Դուք գտնուм ես օրինաչափություններ և հարաբերակցություններ աмողժաмանի տվյալներուм։",
            disclaimer = "ԿԱՌԵՎՈՌ՝ Դուք ՉԵՔ ախտորоոշուм տաмիս։ Օգտագоրծիր ՜հնարավоր հարաբերակցություն՝՝, ՜արժե ուշադրության արժանի՝՝։",
            mainInstruction = { d -> "Վերլուծեք $d օրվա բժշկական տվյալների հարաբերակցություններն ու օրինաչափությունները։ Պատասխանեք հայերեն։" },
            symptomsHeader = "ԱԽՏԱՆԻՇՆԵՌ", vitalsHeader = "ԿԵՆՍԱԿԱՆ ՑՈՒԻՉՆԵՌ",
            findPatterns = "Գտեք օրինաչափություններ, կրկնվող օրինաչափություններ և ախտանիշների ու կենսական ցուցիչների мիժև հնարավоր հարաբերակցություններ։"
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

// --- Disease analysis localization ---

private data class DiseaseAnalysisTexts(
    val systemRole: String,
    val disclaimer: String,
    val mainInstruction: (String) -> String,
    val symptomsHeader: String,
    val vitalsHeader: String,
    val medicationsHeader: String,
    val noSymptoms: String,
    val noVitals: String,
    val noMedications: String,
    val intensity: String,
    val triggers: String,
    val notes: String,
    val analysisRequest: String
)

private object DiseaseAnalysisL10n {
    fun forLanguage(lang: String): DiseaseAnalysisTexts = when (lang) {
        "en" -> DiseaseAnalysisTexts(
            systemRole = "You are a medical data analyst specializing in disease tracking and recovery monitoring. You analyze health journal data to provide insights about disease progression, symptom trends, and recovery indicators.",
            disclaimer = "IMPORTANT: You do NOT diagnose conditions and do NOT prescribe medications. Use phrases like \"the data suggests\", \"possible trend\", \"recommend discussing with your doctor\". Focus on observable patterns in the recorded data.",
            mainInstruction = { disease -> "Analyze the following health data recorded for the condition \"$disease\". Provide a structured analysis covering: 1) Disease progression overview, 2) Symptom severity trends, 3) Vital signs assessment in context of this condition, 4) Medication effectiveness observations, 5) Recovery indicators and recommendations. Respond in English." },
            symptomsHeader = "SYMPTOMS", vitalsHeader = "VITALS", medicationsHeader = "MEDICATIONS",
            noSymptoms = "No symptom records.", noVitals = "No vital signs recorded.", noMedications = "No medications recorded.",
            intensity = "intensity", triggers = "Triggers", notes = "Notes",
            analysisRequest = "Based on the data above, provide a comprehensive disease tracking analysis with actionable insights about progression and recovery."
        )
        "es" -> DiseaseAnalysisTexts(
            systemRole = "Eres un analista de datos m\u00E9dicos especializado en seguimiento de enfermedades y monitoreo de recuperaci\u00F3n.",
            disclaimer = "IMPORTANTE: NO diagnosticas enfermedades y NO recetas medicamentos. Usa frases como \u00ABlos datos sugieren\u00BB, \u00ABposible tendencia\u00BB, \u00ABse recomienda consultar con su m\u00E9dico\u00BB.",
            mainInstruction = { disease -> "Analiza los siguientes datos de salud registrados para la condici\u00F3n \"$disease\". Proporciona un an\u00E1lisis estructurado: 1) Progresi\u00F3n de la enfermedad, 2) Tendencias de s\u00EDntomas, 3) Evaluaci\u00F3n de signos vitales, 4) Efectividad de medicamentos, 5) Indicadores de recuperaci\u00F3n. Responde en espa\u00F1ol." },
            symptomsHeader = "S\u00CDNTOMAS", vitalsHeader = "SIGNOS VITALES", medicationsHeader = "MEDICAMENTOS",
            noSymptoms = "No hay registros de s\u00EDntomas.", noVitals = "No hay registros de signos vitales.", noMedications = "No hay medicamentos registrados.",
            intensity = "intensidad", triggers = "Desencadenantes", notes = "Notas",
            analysisRequest = "Proporciona un an\u00E1lisis integral del seguimiento de la enfermedad con observaciones sobre la progresi\u00F3n y recuperaci\u00F3n."
        )
        "zh-CN" -> DiseaseAnalysisTexts(
            systemRole = "\u4F60\u662F\u4E00\u540D\u4E13\u6CE8\u4E8E\u75BE\u75C5\u8DDF\u8E2A\u548C\u5EB7\u590D\u76D1\u6D4B\u7684\u533B\u7597\u6570\u636E\u5206\u6790\u5E08\u3002",
            disclaimer = "\u91CD\u8981\u63D0\u793A\uFF1A\u4F60\u4E0D\u505A\u8BCA\u65AD\uFF0C\u4E5F\u4E0D\u5F00\u5904\u65B9\u3002\u8BF7\u4F7F\u7528\u300C\u6570\u636E\u663E\u793A\u300D\u3001\u300C\u53EF\u80FD\u7684\u8D8B\u52BF\u300D\u3001\u300C\u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u300D\u7B49\u63AA\u8F9E\u3002",
            mainInstruction = { disease -> "\u5206\u6790\u4E3A\u75C5\u60C5\u300C$disease\u300D\u8BB0\u5F55\u7684\u5065\u5EB7\u6570\u636E\u3002\u63D0\u4F9B\u7ED3\u6784\u5316\u5206\u6790\uFF1A1) \u75C5\u60C5\u8FDB\u5C55, 2) \u75C7\u72B6\u8D8B\u52BF, 3) \u751F\u547D\u4F53\u5F81\u8BC4\u4F30, 4) \u836F\u7269\u6548\u679C, 5) \u5EB7\u590D\u6307\u6807\u3002\u7528\u7B80\u4F53\u4E2D\u6587\u56DE\u590D\u3002" },
            symptomsHeader = "\u75C7\u72B6", vitalsHeader = "\u751F\u547D\u4F53\u5F81", medicationsHeader = "\u836F\u7269",
            noSymptoms = "\u65E0\u75C7\u72B6\u8BB0\u5F55\u3002", noVitals = "\u65E0\u751F\u547D\u4F53\u5F81\u8BB0\u5F55\u3002", noMedications = "\u65E0\u836F\u7269\u8BB0\u5F55\u3002",
            intensity = "\u5F3A\u5EA6", triggers = "\u8BF1\u56E0", notes = "\u5907\u6CE8",
            analysisRequest = "\u63D0\u4F9B\u5168\u9762\u7684\u75BE\u75C5\u8DDF\u8E2A\u5206\u6790\u3002"
        )
        "hy" -> DiseaseAnalysisTexts(
            systemRole = "\u0534\u0578\u0582\u0584 \u0562\u056A\u0577\u056F\u0561\u056F\u0561\u0576 \u057F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u056B \u057E\u0565\u0580\u056C\u0578\u0582\u056E\u0561\u0562\u0561\u0576 \u0565\u057D\u0589",
            disclaimer = "\u053F\u0531\u054C\u0535\u054E\u0548\u054C\u055D \u0534\u0578\u0582\u0584 \u0549\u0535\u0554 \u0561\u056D\u057F\u043E\u0580\u043E\u0577\u0578\u0582\u043C \u0587 \u0549\u0535\u0554 \u0564\u0565\u0572\u0565\u0580 \u0576\u0577\u0561\u0576\u0561\u056F\u0578\u0582\u043C\u0589",
            mainInstruction = { disease -> "\u054E\u0565\u0580\u056C\u0578\u0582\u056E\u0565\u0584 \u057F\u057E\u0575\u0561\u056C\u0576\u0565\u0580\u0568 \u00AB$disease\u00BB \u0570\u056B\u057E\u0561\u0576\u0564\u0578\u0582\u0569\u0575\u0561\u0576 \u0570\u0561\u043C\u0561\u0580\u0589 \u054A\u0561\u057F\u0561\u057D\u056D\u0561\u0576\u0565\u0584 \u0570\u0561\u0575\u0565\u0580\u0565\u0576\u0589" },
            symptomsHeader = "\u0531\u053D\u054F\u0531\u0546\u053B\u0547\u0546\u0535\u054C", vitalsHeader = "\u053F\u0535\u0546\u054D\u0531\u053F\u0531\u0546 \u0551\u0548\u0552\u053B\u0549\u0546\u0535\u054C", medicationsHeader = "\u0534\u0535\u0542\u0535\u054C",
            noSymptoms = "\u0533\u0580\u0561\u0576\u0581\u0578\u0582\u043C\u0576\u0565\u0580 \u0579\u056F\u0561\u0576\u0589", noVitals = "\u0533\u0580\u0561\u0576\u0581\u0578\u0582\u043C\u0576\u0565\u0580 \u0579\u056F\u0561\u0576\u0589", noMedications = "\u0533\u0580\u0561\u0576\u0581\u0578\u0582\u043C\u0576\u0565\u0580 \u0579\u056F\u0561\u0576\u0589",
            intensity = "\u056B\u0576\u057F\u0565\u0576\u057D\u056B\u057E\u0578\u0582\u0569\u0575\u0578\u0582\u0576", triggers = "\u054A\u0561\u057F\u0573\u0561\u057C\u0576\u0565\u0580", notes = "\u0546\u043E\u057F\u0561\u0576\u0565\u0580",
            analysisRequest = "\u054F\u0580\u0561\u043C\u0561\u0564\u0580\u0565\u0584 \u0570\u056B\u057E\u0561\u0576\u0564\u0578\u0582\u0569\u0575\u0561\u0576 \u057E\u0565\u0580\u056C\u0578\u0582\u056E\u0578\u0582\u0569\u0575\u0578\u0582\u0576\u0589"
        )
        else -> DiseaseAnalysisTexts(
            systemRole = "\u0422\u044B \u043C\u0435\u0434\u0438\u0446\u0438\u043D\u0441\u043A\u0438\u0439 \u0430\u043D\u0430\u043B\u0438\u0442\u0438\u043A, \u0441\u043F\u0435\u0446\u0438\u0430\u043B\u0438\u0437\u0438\u0440\u0443\u044E\u0449\u0438\u0439\u0441\u044F \u043D\u0430 \u043E\u0442\u0441\u043B\u0435\u0436\u0438\u0432\u0430\u043D\u0438\u0438 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u0439 \u0438 \u043C\u043E\u043D\u0438\u0442\u043E\u0440\u0438\u043D\u0433\u0435 \u0432\u044B\u0437\u0434\u043E\u0440\u043E\u0432\u043B\u0435\u043D\u0438\u044F.",
            disclaimer = "\u0412\u0410\u0416\u041D\u041E: \u0422\u044B \u041D\u0415 \u0441\u0442\u0430\u0432\u0438\u0448\u044C \u0434\u0438\u0430\u0433\u043D\u043E\u0437\u044B \u0438 \u041D\u0415 \u043D\u0430\u0437\u043D\u0430\u0447\u0430\u0435\u0448\u044C \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432\u0430. \u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0439 \u0444\u043E\u0440\u043C\u0443\u043B\u0438\u0440\u043E\u0432\u043A\u0438 \u00AB\u0434\u0430\u043D\u043D\u044B\u0435 \u0443\u043A\u0430\u0437\u044B\u0432\u0430\u044E\u0442\u00BB, \u00AB\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u0430\u044F \u0442\u0435\u043D\u0434\u0435\u043D\u0446\u0438\u044F\u00BB, \u00AB\u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C\u00BB.",
            mainInstruction = { disease -> "\u041F\u0440\u043E\u0430\u043D\u0430\u043B\u0438\u0437\u0438\u0440\u0443\u0439 \u0434\u0430\u043D\u043D\u044B\u0435 \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u044F \u0434\u043B\u044F \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F \u00AB$disease\u00BB. \u041F\u0440\u0435\u0434\u043E\u0441\u0442\u0430\u0432\u044C: 1) \u041E\u0431\u0437\u043E\u0440 \u0434\u0438\u043D\u0430\u043C\u0438\u043A\u0438, 2) \u0422\u0435\u043D\u0434\u0435\u043D\u0446\u0438\u0438 \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u043E\u0432, 3) \u041E\u0446\u0435\u043D\u043A\u0430 \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u0435\u0439, 4) \u042D\u0444\u0444\u0435\u043A\u0442\u0438\u0432\u043D\u043E\u0441\u0442\u044C \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432, 5) \u0418\u043D\u0434\u0438\u043A\u0430\u0442\u043E\u0440\u044B \u0432\u044B\u0437\u0434\u043E\u0440\u043E\u0432\u043B\u0435\u043D\u0438\u044F. \u041E\u0442\u0432\u0435\u0447\u0430\u0439 \u043D\u0430 \u0440\u0443\u0441\u0441\u043A\u043E\u043C." },
            symptomsHeader = "\u0421\u0418\u041C\u041F\u0422\u041E\u041C\u042B", vitalsHeader = "\u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0418", medicationsHeader = "\u041B\u0415\u041A\u0410\u0420\u0421\u0422\u0412\u0410",
            noSymptoms = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u0430\u0445.", noVitals = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u044F\u0445.", noMedications = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432\u0430\u0445.",
            intensity = "\u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C", triggers = "\u0422\u0440\u0438\u0433\u0433\u0435\u0440\u044B", notes = "\u0417\u0430\u043C\u0435\u0442\u043A\u0438",
            analysisRequest = "\u041F\u0440\u0435\u0434\u043E\u0441\u0442\u0430\u0432\u044C \u043A\u043E\u043C\u043F\u043B\u0435\u043A\u0441\u043D\u044B\u0439 \u0430\u043D\u0430\u043B\u0438\u0437 \u0434\u0438\u043D\u0430\u043C\u0438\u043A\u0438 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F \u0438 \u0432\u044B\u0437\u0434\u043E\u0440\u043E\u0432\u043B\u0435\u043D\u0438\u044F."
        )
    }
}
