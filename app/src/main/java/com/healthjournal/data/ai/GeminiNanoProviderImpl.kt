package com.healthjournal.data.ai

import android.content.Context
import android.os.Build
import com.google.ai.edge.aicore.GenerationConfig as AiCoreGenerationConfig
import com.google.ai.edge.aicore.generationConfig as aiCoreGenerationConfig
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.healthjournal.R
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.format.DateTimeFormatter

/**
 * On-device AI provider combining Google AI Edge SDK and ML Kit GenAI Prompt API.
 * Falls back to Samsung Galaxy AI or local rule-based analysis when unavailable.
 * Priority: AI Edge SDK → ML Kit GenAI → Samsung Galaxy AI → local analysis.
 */
class GeminiNanoProviderImpl(
    private val context: Context
) : AiProvider {

    override val id = AiProviderId.GEMINI_NANO
    override val displayNameResId = R.string.ai_provider_gemini_nano

    private var aiEdgeModel: com.google.ai.edge.aicore.GenerativeModel? = null
    private var mlKitModel: GenerativeModel? = null

    private fun getOrCreateMlKitModel(): GenerativeModel {
        return mlKitModel ?: Generation.getClient().also { mlKitModel = it }
    }

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = tryRunInference(fullPrompt, config.geminiNanoConfig)
        return if (result != null) {
            AiTextResult(text = result.first, providerId = id, modelUsed = result.second)
        } else {
            AiTextResult(text = buildLocalSummary(input), providerId = id, modelUsed = "local-analysis")
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = tryRunInference(fullPrompt, config.geminiNanoConfig)
        return if (result != null) {
            AiFlagsResult(text = result.first, providerId = id, modelUsed = result.second)
        } else {
            AiFlagsResult(text = buildLocalPatternAnalysis(input), providerId = id, modelUsed = "local-analysis")
        }
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val availability = checkAvailability()
        return ValidationResult(true, "Available: ${availability.label}")
    }

    override fun isOnlineRequired(): Boolean = false

    // ==================== AI SDK inference ====================

    private suspend fun tryRunInference(
        prompt: String,
        config: GeminiNanoConfig
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        // 1. Google AI Edge SDK (Gemini Nano via AICore, API 31+)
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                return@withContext runViaAiEdge(prompt, config) to "gemini-nano-aicore"
            } catch (_: Exception) { }
        }
        // 2. ML Kit GenAI Prompt API (Gemini Nano, broader device support)
        try {
            return@withContext runViaMlKit(prompt, config) to "gemini-nano"
        } catch (_: Exception) { }
        // 3. Samsung Galaxy AI - Samsung-specific fallback
        try {
            return@withContext runViaSamsungAi(prompt, config) to "galaxy-ai"
        } catch (_: Exception) { }
        null
    }

    /**
     * Run inference via Google AI Edge SDK (AICore).
     * Requires API 31+ and Google AI Core service on the device.
     */
    private suspend fun runViaAiEdge(prompt: String, config: GeminiNanoConfig): String {
        val model = aiEdgeModel ?: run {
            val genConfig = aiCoreGenerationConfig {
                context = this@GeminiNanoProviderImpl.context
                temperature = config.temperature
                topK = config.topK
                maxOutputTokens = config.maxOutputTokens
            }
            com.google.ai.edge.aicore.GenerativeModel(genConfig).also { aiEdgeModel = it }
        }
        val response = model.generateContent(prompt)
        return response.text ?: throw RuntimeException("Empty response from AI Edge")
    }

    /**
     * Run inference via ML Kit GenAI Prompt API (Gemini Nano on-device).
     * Handles model download if the model is available but not yet downloaded.
     */
    private suspend fun runViaMlKit(prompt: String, config: GeminiNanoConfig): String {
        val model = getOrCreateMlKitModel()
        val status = model.checkStatus()

        if (status == FeatureStatus.UNAVAILABLE) {
            throw RuntimeException("Gemini Nano is not available on this device")
        }

        if (status != FeatureStatus.AVAILABLE) {
            // Model needs downloading - wait up to 2 minutes
            withTimeout(120_000) {
                model.download().collect { ds ->
                    if (ds is DownloadStatus.DownloadFailed) throw ds.e
                }
            }
            if (model.checkStatus() != FeatureStatus.AVAILABLE) {
                throw RuntimeException("Gemini Nano model not available after download")
            }
        }

        val request = generateContentRequest(TextPart(prompt)) {
            temperature = config.temperature
            topK = config.topK
            maxOutputTokens = config.maxOutputTokens
        }
        val response = model.generateContent(request)
        return response.candidates.firstOrNull()?.text
            ?: throw RuntimeException("Empty response from Gemini Nano")
    }

    private fun runViaSamsungAi(prompt: String, config: GeminiNanoConfig): String {
        try {
            val engineClass = Class.forName("com.samsung.android.sdk.aiinference.InferenceEngine")
            val engine = engineClass.getMethod("getInstance", Context::class.java)
                .invoke(null, context)
            val response = engineClass.getMethod("generateText", String::class.java)
                .invoke(engine, prompt)
            val result = response as? String
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) { }

        try {
            val modelClass = Class.forName("com.samsung.android.sdk.ai.model.GenAIModel")
            val getInstance = modelClass.getMethod("getInstance", Context::class.java)
            val model = getInstance.invoke(null, context)
            val response = modelClass.getMethod("generate", String::class.java)
                .invoke(model, prompt)
            val result = response as? String
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) { }

        val providers = listOf(
            "content://com.samsung.android.aicoreondevice/generate",
            "content://com.samsung.android.galaxyai.provider/generate",
            "content://com.samsung.android.intelligence/generate"
        )
        for (uri in providers) {
            try {
                val cursor = context.contentResolver.query(
                    android.net.Uri.parse(uri), null, prompt, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val result = it.getString(0)
                        if (!result.isNullOrBlank()) return result
                    }
                }
            } catch (_: Exception) { }
        }

        throw RuntimeException("No Samsung AI SDK path available")
    }

    // ==================== Availability detection ====================

    private enum class AvailabilityStatus(val label: String) {
        AI_EDGE("Gemini Nano (AI Edge)"),
        MLKIT_GENAI("Gemini Nano (ML Kit)"),
        SAMSUNG_GALAXY_AI("Samsung Galaxy AI"),
        LOCAL_ANALYSIS("Local analysis")
    }

    private fun checkAvailability(): AvailabilityStatus {
        val pm = context.packageManager

        // Check for Google AI Core service (required for AI Edge SDK and ML Kit GenAI)
        try {
            pm.getPackageInfo("com.google.android.aicore", 0)
            return if (Build.VERSION.SDK_INT >= 31) AvailabilityStatus.AI_EDGE
            else AvailabilityStatus.MLKIT_GENAI
        } catch (_: Exception) { }

        // Check for Samsung AI packages
        for (pkg in listOf(
            "com.samsung.android.aicoreondevice",
            "com.samsung.android.galaxyai",
            "com.samsung.android.intelligence"
        )) {
            try { pm.getPackageInfo(pkg, 0); return AvailabilityStatus.SAMSUNG_GALAXY_AI }
            catch (_: Exception) { }
        }

        // Infer from manufacturer
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer == "google") {
            return if (Build.VERSION.SDK_INT >= 31) AvailabilityStatus.AI_EDGE
            else AvailabilityStatus.MLKIT_GENAI
        }
        if (manufacturer == "samsung") return AvailabilityStatus.SAMSUNG_GALAXY_AI

        return AvailabilityStatus.LOCAL_ANALYSIS
    }

    // ==================== Local analysis engine ====================

    private enum class TrendDirection { UP, DOWN, STABLE }

    private fun detectTrend(values: List<Double>): TrendDirection {
        if (values.size < 2) return TrendDirection.STABLE
        val n = values.size
        val firstHalf = values.take(n / 2 + n % 2).average()
        val secondHalf = values.drop(n / 2 + n % 2).average()
        val avg = values.average()
        val threshold = if (avg != 0.0) avg * 0.05 else 1.0
        return when {
            secondHalf - firstHalf > threshold -> TrendDirection.UP
            firstHalf - secondHalf > threshold -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
    }

    private fun formatVitalValue(value: Double, secondary: Double?, type: VitalType): String =
        if (type == VitalType.BLOOD_PRESSURE && secondary != null) {
            "${value.toInt()}/${secondary.toInt()}"
        } else if (type == VitalType.TEMPERATURE || type == VitalType.GLUCOSE) {
            "%.1f".format(value)
        } else {
            value.toInt().toString()
        }

    private fun formatValue(value: Double, type: VitalType): String =
        if (type == VitalType.TEMPERATURE || type == VitalType.GLUCOSE) {
            "%.1f".format(value)
        } else {
            value.toInt().toString()
        }

    private fun checkVitalRange(value: Double, secondary: Double?, type: VitalType, t: AnalysisTexts): String {
        return when (type) {
            VitalType.BLOOD_PRESSURE -> {
                val flags = mutableListOf<String>()
                if (value >= 140) flags.add(t.highBP)
                else if (value < 90) flags.add(t.lowBP)
                if (secondary != null) {
                    if (secondary >= 90) flags.add(t.highDiastolic)
                    else if (secondary < 60) flags.add(t.lowDiastolic)
                }
                flags.joinToString(", ")
            }
            VitalType.PULSE -> when {
                value > 100 -> t.highPulse
                value < 60 -> t.lowPulse
                else -> ""
            }
            VitalType.TEMPERATURE -> when {
                value >= 38.0 -> t.fever
                value >= 37.5 -> t.subfebrile
                value < 35.5 -> t.hypothermia
                else -> ""
            }
            VitalType.SPO2 -> when {
                value < 90 -> t.criticalO2
                value < 95 -> t.lowO2
                else -> ""
            }
            VitalType.GLUCOSE -> when {
                value > 11.0 -> t.highGlucose
                value > 7.8 -> t.elevatedGlucose
                value < 3.9 -> t.lowGlucose
                else -> ""
            }
            else -> ""
        }
    }

    private fun generateObservations(input: AiInput, t: AnalysisTexts): List<String> {
        val obs = mutableListOf<String>()

        val severe = input.symptoms.filter { it.intensity >= 7 }
        if (severe.isNotEmpty()) {
            val names = severe.map { it.name }.distinct().joinToString(", ")
            obs.add("${t.severeSymptoms}: $names")
        }

        val symptomGroups = input.symptoms.groupBy { it.name }
        symptomGroups.filter { it.value.size >= 3 }.forEach { (name, occurrences) ->
            obs.add("${t.recurringSymptom}: $name (${occurrences.size}x)")
        }

        val abnormalVitals = mutableSetOf<String>()
        input.vitals.forEach { v ->
            val flag = checkVitalRange(v.value, v.secondaryValue, v.type, t)
            if (flag.isNotEmpty()) {
                val valueStr = formatVitalValue(v.value, v.secondaryValue, v.type)
                val key = "${v.type.displayName} $valueStr"
                if (abnormalVitals.add(key)) {
                    obs.add("${v.type.displayName} $valueStr ${v.type.unit}: $flag")
                }
            }
        }

        if (input.knownDiseases.isNotEmpty()) {
            val diseases = input.knownDiseases.map { it.lowercase() }
            val isDiabetes = diseases.any {
                it.contains("\u0434\u0438\u0430\u0431\u0435\u0442") || it.contains("diabetes") ||
                    it.contains("\u7CD6\u5C3F\u75C5") || it.contains("diabet")
            }
            if (isDiabetes) {
                val highGlucose = input.vitals.filter { it.type == VitalType.GLUCOSE && it.value > 7.8 }
                if (highGlucose.isNotEmpty()) {
                    obs.add(t.diabetesGlucoseWarning)
                }
            }
            val isHypertension = diseases.any {
                it.contains("\u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D") || it.contains("hypertens") ||
                    it.contains("\u9AD8\u8840\u538B") || it.contains("hipertens")
            }
            if (isHypertension) {
                val highBp = input.vitals.filter { it.type == VitalType.BLOOD_PRESSURE && it.value >= 140 }
                if (highBp.isNotEmpty()) {
                    obs.add(t.hypertensionBPWarning)
                }
            }
        }

        return obs
    }

    private fun findCorrelations(input: AiInput, t: AnalysisTexts): List<String> {
        val correlations = mutableListOf<String>()
        if (input.symptoms.isEmpty() || input.vitals.isEmpty()) return correlations

        val symptomDays = input.symptoms.groupBy { it.recordedAt.toLocalDate() }
        val vitalDays = input.vitals.groupBy { it.recordedAt.toLocalDate() }

        for ((date, symptoms) in symptomDays) {
            val vitalsOnDay = vitalDays[date] ?: continue
            val abnormal = vitalsOnDay.filter { v ->
                checkVitalRange(v.value, v.secondaryValue, v.type, t).isNotEmpty()
            }
            if (abnormal.isNotEmpty()) {
                val symptomNames = symptoms.map { it.name }.distinct().joinToString(", ")
                val vitalNames = abnormal.map { it.type.displayName }.distinct().joinToString(", ")
                correlations.add("${t.sameDayCorrelation}: $symptomNames + $vitalNames ($date)")
            }
        }

        return correlations.distinct().take(5)
    }

    // ---- Local summary ----

    private fun buildLocalSummary(input: AiInput): String {
        val t = AnalysisTexts.forLanguage(input.outputLanguage)
        val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return buildString {
            appendLine(t.summaryTitle)
            appendLine(t.localAnalysisNote)
            appendLine()

            // Patient info
            if (input.weight.isNotBlank() || input.height.isNotBlank() || input.knownDiseases.isNotEmpty()) {
                appendLine("=== ${t.patientInfo} ===")
                if (input.weight.isNotBlank()) appendLine("${t.weightLabel}: ${input.weight}")
                if (input.height.isNotBlank()) appendLine("${t.heightLabel}: ${input.height}")
                if (input.knownDiseases.isNotEmpty()) {
                    appendLine("${t.knownConditions}: ${input.knownDiseases.joinToString(", ")}")
                }
                appendLine()
            }

            // Symptoms
            appendLine("=== ${t.symptomsSection} ===")
            if (input.symptoms.isEmpty()) {
                appendLine(t.noSymptoms)
            } else {
                input.symptoms.sortedByDescending { it.recordedAt }.forEach { s ->
                    append("\u2022 ${s.name} \u2014 ${t.intensity}: ${s.intensity}/10")
                    appendLine(" (${s.recordedAt.format(dateFmt)})")
                    if (s.triggers.isNotEmpty()) appendLine("  ${t.triggers}: ${s.triggers.joinToString(", ")}")
                    if (s.notes.isNotBlank()) appendLine("  ${t.notes}: ${s.notes}")
                }
            }
            appendLine()

            // Vitals
            appendLine("=== ${t.vitalsSection} ===")
            if (input.vitals.isEmpty()) {
                appendLine(t.noVitals)
            } else {
                val grouped = input.vitals.groupBy { it.type }
                for ((type, vitals) in grouped) {
                    val sorted = vitals.sortedByDescending { it.recordedAt }
                    appendLine("\u25B8 ${type.displayName}:")
                    sorted.forEach { v ->
                        val valueStr = formatVitalValue(v.value, v.secondaryValue, type)
                        val flag = checkVitalRange(v.value, v.secondaryValue, type, t)
                        val flagStr = if (flag.isNotEmpty()) " \u26A0 $flag" else ""
                        appendLine("  ${v.recordedAt.format(dateFmt)}: $valueStr ${type.unit}$flagStr")
                    }
                    if (sorted.size >= 2) {
                        val trend = detectTrend(sorted.reversed().map { it.value })
                        val trendLabel = when (trend) {
                            TrendDirection.UP -> t.trendUp
                            TrendDirection.DOWN -> t.trendDown
                            TrendDirection.STABLE -> t.trendStable
                        }
                        appendLine("  ${t.trend}: $trendLabel")
                    }
                    appendLine()
                }
            }

            // Medications
            appendLine("=== ${t.medicationsSection} ===")
            if (input.medications.isEmpty()) {
                appendLine(t.noMedications)
            } else {
                input.medications.forEach { m ->
                    appendLine("\u2022 ${m.name} \u2014 ${m.dosage}, ${m.frequency}")
                }
            }
            appendLine()

            // Observations
            val observations = generateObservations(input, t)
            if (observations.isNotEmpty()) {
                appendLine("=== ${t.observationsSection} ===")
                observations.forEach { appendLine("\u2022 $it") }
                appendLine()
            }

            appendLine(t.disclaimer)
        }
    }

    // ---- Local pattern analysis ----

    private fun buildLocalPatternAnalysis(input: AiInput): String {
        val t = AnalysisTexts.forLanguage(input.outputLanguage)
        val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return buildString {
            appendLine(t.patternTitle)
            appendLine(t.localAnalysisNote)
            appendLine()

            // Vital trends
            val vitalGroups = input.vitals.groupBy { it.type }
            if (vitalGroups.any { it.value.size >= 2 }) {
                appendLine("=== ${t.vitalTrends} ===")
                for ((type, vitals) in vitalGroups) {
                    val sorted = vitals.sortedBy { it.recordedAt }
                    if (sorted.size < 2) continue

                    val values = sorted.map { it.value }
                    val trend = detectTrend(values)
                    val min = values.min()
                    val max = values.max()
                    val avg = values.average()

                    val trendLabel = when (trend) {
                        TrendDirection.UP -> "\u2191 ${t.trendUp}"
                        TrendDirection.DOWN -> "\u2193 ${t.trendDown}"
                        TrendDirection.STABLE -> "\u2192 ${t.trendStable}"
                    }

                    appendLine("\u25B8 ${type.displayName}: $trendLabel")
                    appendLine("  ${t.minValue}: ${formatValue(min, type)} | " +
                        "${t.maxValue}: ${formatValue(max, type)} | " +
                        "${t.avgValue}: ${formatValue(avg, type)} ${type.unit}")

                    if (type == VitalType.BLOOD_PRESSURE) {
                        val diastolicValues = sorted.mapNotNull { it.secondaryValue }
                        if (diastolicValues.size >= 2) {
                            val dTrend = detectTrend(diastolicValues)
                            val dLabel = when (dTrend) {
                                TrendDirection.UP -> "\u2191 ${t.trendUp}"
                                TrendDirection.DOWN -> "\u2193 ${t.trendDown}"
                                TrendDirection.STABLE -> "\u2192 ${t.trendStable}"
                            }
                            appendLine("  ${t.diastolicLabel}: $dLabel " +
                                "(${diastolicValues.min().toInt()}-${diastolicValues.max().toInt()} ${type.unit})")
                        }
                    }

                    val abnormal = sorted.filter { v ->
                        checkVitalRange(v.value, v.secondaryValue, type, t).isNotEmpty()
                    }
                    if (abnormal.isNotEmpty()) {
                        appendLine("  \u26A0 ${t.abnormalReadings}: ${abnormal.size}/${sorted.size}")
                    }
                    appendLine()
                }
            }

            // Symptom frequency
            if (input.symptoms.isNotEmpty()) {
                appendLine("=== ${t.symptomFrequency} ===")
                val symptomGroups = input.symptoms.groupBy { it.name }
                val sortedGroups = symptomGroups.entries.sortedByDescending { it.value.size }

                for ((name, occurrences) in sortedGroups) {
                    val count = occurrences.size
                    val avgIntensity = occurrences.map { it.intensity }.average()
                    val maxIntensity = occurrences.maxOf { it.intensity }

                    appendLine("\u25B8 $name: ${count}x")
                    appendLine("  ${t.avgIntensity}: ${"%.1f".format(avgIntensity)}/10, ${t.maxIntensity}: $maxIntensity/10")

                    val allTriggers = occurrences.flatMap { it.triggers }
                    if (allTriggers.isNotEmpty()) {
                        val triggerCounts = allTriggers.groupingBy { it }.eachCount()
                            .entries.sortedByDescending { it.value }
                        appendLine("  ${t.commonTriggers}: ${triggerCounts.joinToString(", ") { "${it.key} (${it.value}x)" }}")
                    }

                    if (count >= 2) {
                        val dates = occurrences.sortedBy { it.recordedAt }
                        appendLine("  ${t.period}: ${dates.first().recordedAt.format(dateFmt)} \u2014 ${dates.last().recordedAt.format(dateFmt)}")
                    }
                    appendLine()
                }
            }

            // Correlations
            val correlations = findCorrelations(input, t)
            if (correlations.isNotEmpty()) {
                appendLine("=== ${t.correlationsSection} ===")
                correlations.forEach { appendLine("\u2022 $it") }
                appendLine()
            }

            if (input.symptoms.isEmpty() && input.vitals.isEmpty()) {
                appendLine(t.noPatterns)
                appendLine()
            }

            appendLine(t.disclaimer)
        }
    }
}

// ==================== Localized analysis texts ====================

private data class AnalysisTexts(
    // Headers
    val summaryTitle: String,
    val patternTitle: String,
    val localAnalysisNote: String,
    val patientInfo: String,
    val symptomsSection: String,
    val vitalsSection: String,
    val medicationsSection: String,
    val observationsSection: String,
    val correlationsSection: String,
    val vitalTrends: String,
    val symptomFrequency: String,
    // Labels
    val weightLabel: String,
    val heightLabel: String,
    val knownConditions: String,
    val intensity: String,
    val triggers: String,
    val notes: String,
    val trend: String,
    val trendUp: String,
    val trendDown: String,
    val trendStable: String,
    val minValue: String,
    val maxValue: String,
    val avgValue: String,
    val avgIntensity: String,
    val maxIntensity: String,
    val commonTriggers: String,
    val abnormalReadings: String,
    val period: String,
    val diastolicLabel: String,
    // Empty states
    val noSymptoms: String,
    val noVitals: String,
    val noMedications: String,
    val noPatterns: String,
    // Vital range flags
    val highBP: String,
    val lowBP: String,
    val highDiastolic: String,
    val lowDiastolic: String,
    val highPulse: String,
    val lowPulse: String,
    val fever: String,
    val subfebrile: String,
    val hypothermia: String,
    val criticalO2: String,
    val lowO2: String,
    val highGlucose: String,
    val elevatedGlucose: String,
    val lowGlucose: String,
    // Observations
    val severeSymptoms: String,
    val recurringSymptom: String,
    val sameDayCorrelation: String,
    val diabetesGlucoseWarning: String,
    val hypertensionBPWarning: String,
    val disclaimer: String
) {
    companion object {
        val EN = AnalysisTexts(
            summaryTitle = "[Health Data Summary]",
            patternTitle = "[Pattern Analysis]",
            localAnalysisNote = "Generated by local analysis engine (no AI SDK available on this device).",
            patientInfo = "PATIENT INFO",
            symptomsSection = "SYMPTOMS",
            vitalsSection = "VITAL SIGNS",
            medicationsSection = "MEDICATIONS",
            observationsSection = "OBSERVATIONS",
            correlationsSection = "CORRELATIONS",
            vitalTrends = "VITAL SIGN TRENDS",
            symptomFrequency = "SYMPTOM FREQUENCY",
            weightLabel = "Weight",
            heightLabel = "Height",
            knownConditions = "Known conditions",
            intensity = "intensity",
            triggers = "Triggers",
            notes = "Notes",
            trend = "Trend",
            trendUp = "increasing",
            trendDown = "decreasing",
            trendStable = "stable",
            minValue = "Min",
            maxValue = "Max",
            avgValue = "Avg",
            avgIntensity = "Avg intensity",
            maxIntensity = "Max intensity",
            commonTriggers = "Common triggers",
            abnormalReadings = "Abnormal readings",
            period = "Period",
            diastolicLabel = "Diastolic",
            noSymptoms = "No symptoms recorded for this period.",
            noVitals = "No vital signs recorded for this period.",
            noMedications = "No active medications.",
            noPatterns = "Not enough data for pattern analysis.",
            highBP = "elevated (hypertension)",
            lowBP = "low (hypotension)",
            highDiastolic = "diastolic elevated",
            lowDiastolic = "diastolic low",
            highPulse = "elevated (tachycardia)",
            lowPulse = "low (bradycardia)",
            fever = "fever",
            subfebrile = "slightly elevated",
            hypothermia = "low temperature",
            criticalO2 = "critically low oxygen",
            lowO2 = "low oxygen saturation",
            highGlucose = "significantly elevated glucose",
            elevatedGlucose = "elevated glucose",
            lowGlucose = "low glucose (hypoglycemia)",
            severeSymptoms = "Severe symptoms (7+/10)",
            recurringSymptom = "Recurring symptom",
            sameDayCorrelation = "Same-day correlation",
            diabetesGlucoseWarning = "Note: Elevated glucose readings with known diabetes \u2014 recommend discussing with your doctor.",
            hypertensionBPWarning = "Note: Elevated blood pressure with known hypertension \u2014 recommend discussing with your doctor.",
            disclaimer = "This analysis was generated locally without AI. For a more detailed analysis, configure a cloud AI provider in Settings > AI Settings."
        )

        val RU = AnalysisTexts(
            summaryTitle = "[\u0421\u0432\u043E\u0434\u043A\u0430 \u0434\u0430\u043D\u043D\u044B\u0445 \u043E \u0437\u0434\u043E\u0440\u043E\u0432\u044C\u0435]",
            patternTitle = "[\u0410\u043D\u0430\u043B\u0438\u0437 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u043E\u0432]",
            localAnalysisNote = "\u0421\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043E \u043B\u043E\u043A\u0430\u043B\u044C\u043D\u044B\u043C \u0430\u043D\u0430\u043B\u0438\u0437\u0430\u0442\u043E\u0440\u043E\u043C (AI SDK \u043D\u0435\u0434\u043E\u0441\u0442\u0443\u043F\u0435\u043D \u043D\u0430 \u044D\u0442\u043E\u043C \u0443\u0441\u0442\u0440\u043E\u0439\u0441\u0442\u0432\u0435).",
            patientInfo = "\u0418\u041D\u0424\u041E \u041E \u041F\u0410\u0426\u0418\u0415\u041D\u0422\u0415",
            symptomsSection = "\u0421\u0418\u041C\u041F\u0422\u041E\u041C\u042B",
            vitalsSection = "\u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0418",
            medicationsSection = "\u041B\u0415\u041A\u0410\u0420\u0421\u0422\u0412\u0410",
            observationsSection = "\u041D\u0410\u0411\u041B\u042E\u0414\u0415\u041D\u0418\u042F",
            correlationsSection = "\u041A\u041E\u0420\u0420\u0415\u041B\u042F\u0426\u0418\u0418",
            vitalTrends = "\u0422\u0420\u0415\u041D\u0414\u042B \u041F\u041E\u041A\u0410\u0417\u0410\u0422\u0415\u041B\u0415\u0419",
            symptomFrequency = "\u0427\u0410\u0421\u0422\u041E\u0422\u0410 \u0421\u0418\u041C\u041F\u0422\u041E\u041C\u041E\u0412",
            weightLabel = "\u0412\u0435\u0441",
            heightLabel = "\u0420\u043E\u0441\u0442",
            knownConditions = "\u0418\u0437\u0432\u0435\u0441\u0442\u043D\u044B\u0435 \u0437\u0430\u0431\u043E\u043B\u0435\u0432\u0430\u043D\u0438\u044F",
            intensity = "\u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            triggers = "\u0422\u0440\u0438\u0433\u0433\u0435\u0440\u044B",
            notes = "\u0417\u0430\u043C\u0435\u0442\u043A\u0438",
            trend = "\u0422\u0440\u0435\u043D\u0434",
            trendUp = "\u0440\u043E\u0441\u0442",
            trendDown = "\u0441\u043D\u0438\u0436\u0435\u043D\u0438\u0435",
            trendStable = "\u0441\u0442\u0430\u0431\u0438\u043B\u044C\u043D\u043E",
            minValue = "\u041C\u0438\u043D",
            maxValue = "\u041C\u0430\u043A\u0441",
            avgValue = "\u0421\u0440\u0435\u0434\u043D",
            avgIntensity = "\u0421\u0440. \u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            maxIntensity = "\u041C\u0430\u043A\u0441. \u0438\u043D\u0442\u0435\u043D\u0441\u0438\u0432\u043D\u043E\u0441\u0442\u044C",
            commonTriggers = "\u0427\u0430\u0441\u0442\u044B\u0435 \u0442\u0440\u0438\u0433\u0433\u0435\u0440\u044B",
            abnormalReadings = "\u041E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u0438\u044F \u043E\u0442 \u043D\u043E\u0440\u043C\u044B",
            period = "\u041F\u0435\u0440\u0438\u043E\u0434",
            diastolicLabel = "\u0414\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435",
            noSymptoms = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u0430\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.",
            noVitals = "\u041D\u0435\u0442 \u0437\u0430\u043F\u0438\u0441\u0435\u0439 \u043E \u043F\u043E\u043A\u0430\u0437\u0430\u0442\u0435\u043B\u044F\u0445 \u0437\u0430 \u043F\u0435\u0440\u0438\u043E\u0434.",
            noMedications = "\u041D\u0435\u0442 \u0430\u043A\u0442\u0438\u0432\u043D\u044B\u0445 \u043B\u0435\u043A\u0430\u0440\u0441\u0442\u0432.",
            noPatterns = "\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0434\u0430\u043D\u043D\u044B\u0445 \u0434\u043B\u044F \u0430\u043D\u0430\u043B\u0438\u0437\u0430 \u043F\u0430\u0442\u0442\u0435\u0440\u043D\u043E\u0432.",
            highBP = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u043E\u0435 (\u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D\u0438\u044F)",
            lowBP = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u043E\u0435 (\u0433\u0438\u043F\u043E\u0442\u043E\u043D\u0438\u044F)",
            highDiastolic = "\u0434\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435 \u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043E",
            lowDiastolic = "\u0434\u0438\u0430\u0441\u0442\u043E\u043B\u0438\u0447\u0435\u0441\u043A\u043E\u0435 \u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043E",
            highPulse = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u044B\u0439 (\u0442\u0430\u0445\u0438\u043A\u0430\u0440\u0434\u0438\u044F)",
            lowPulse = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u044B\u0439 (\u0431\u0440\u0430\u0434\u0438\u043A\u0430\u0440\u0434\u0438\u044F)",
            fever = "\u043B\u0438\u0445\u043E\u0440\u0430\u0434\u043A\u0430",
            subfebrile = "\u0441\u0443\u0431\u0444\u0435\u0431\u0440\u0438\u043B\u044C\u043D\u0430\u044F",
            hypothermia = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0442\u0435\u043C\u043F\u0435\u0440\u0430\u0442\u0443\u0440\u0430",
            criticalO2 = "\u043A\u0440\u0438\u0442\u0438\u0447\u0435\u0441\u043A\u0438 \u043D\u0438\u0437\u043A\u0438\u0439 \u043A\u0438\u0441\u043B\u043E\u0440\u043E\u0434",
            lowO2 = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0441\u0430\u0442\u0443\u0440\u0430\u0446\u0438\u044F",
            highGlucose = "\u0437\u043D\u0430\u0447\u0438\u0442\u0435\u043B\u044C\u043D\u043E \u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430",
            elevatedGlucose = "\u043F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430",
            lowGlucose = "\u043F\u043E\u043D\u0438\u0436\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430 (\u0433\u0438\u043F\u043E\u0433\u043B\u0438\u043A\u0435\u043C\u0438\u044F)",
            severeSymptoms = "\u0422\u044F\u0436\u0451\u043B\u044B\u0435 \u0441\u0438\u043C\u043F\u0442\u043E\u043C\u044B (7+/10)",
            recurringSymptom = "\u041F\u043E\u0432\u0442\u043E\u0440\u044F\u044E\u0449\u0438\u0439\u0441\u044F \u0441\u0438\u043C\u043F\u0442\u043E\u043C",
            sameDayCorrelation = "\u0421\u043E\u0432\u043F\u0430\u0434\u0435\u043D\u0438\u0435 \u0432 \u043E\u0434\u0438\u043D \u0434\u0435\u043D\u044C",
            diabetesGlucoseWarning = "\u041F\u0440\u0438\u043C\u0435\u0447\u0430\u043D\u0438\u0435: \u041F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u0430\u044F \u0433\u043B\u044E\u043A\u043E\u0437\u0430 \u043F\u0440\u0438 \u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u043C \u0434\u0438\u0430\u0431\u0435\u0442\u0435 \u2014 \u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C.",
            hypertensionBPWarning = "\u041F\u0440\u0438\u043C\u0435\u0447\u0430\u043D\u0438\u0435: \u041F\u043E\u0432\u044B\u0448\u0435\u043D\u043D\u043E\u0435 \u0434\u0430\u0432\u043B\u0435\u043D\u0438\u0435 \u043F\u0440\u0438 \u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u0439 \u0433\u0438\u043F\u0435\u0440\u0442\u043E\u043D\u0438\u0438 \u2014 \u0440\u0435\u043A\u043E\u043C\u0435\u043D\u0434\u0443\u0435\u0442\u0441\u044F \u043E\u0431\u0441\u0443\u0434\u0438\u0442\u044C \u0441 \u0432\u0440\u0430\u0447\u043E\u043C.",
            disclaimer = "\u042D\u0442\u043E\u0442 \u0430\u043D\u0430\u043B\u0438\u0437 \u0441\u0433\u0435\u043D\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u043D \u043B\u043E\u043A\u0430\u043B\u044C\u043D\u043E \u0431\u0435\u0437 \u0418\u0418. \u0414\u043B\u044F \u0431\u043E\u043B\u0435\u0435 \u043F\u043E\u0434\u0440\u043E\u0431\u043D\u043E\u0433\u043E \u0430\u043D\u0430\u043B\u0438\u0437\u0430 \u043D\u0430\u0441\u0442\u0440\u043E\u0439\u0442\u0435 \u043E\u0431\u043B\u0430\u0447\u043D\u043E\u0433\u043E AI-\u043F\u0440\u043E\u0432\u0430\u0439\u0434\u0435\u0440\u0430 \u0432 \u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438 > AI."
        )

        val ES = AnalysisTexts(
            summaryTitle = "[Resumen de datos de salud]",
            patternTitle = "[An\u00E1lisis de patrones]",
            localAnalysisNote = "Generado por el motor de an\u00E1lisis local (SDK de IA no disponible en este dispositivo).",
            patientInfo = "INFO DEL PACIENTE",
            symptomsSection = "S\u00CDNTOMAS",
            vitalsSection = "SIGNOS VITALES",
            medicationsSection = "MEDICAMENTOS",
            observationsSection = "OBSERVACIONES",
            correlationsSection = "CORRELACIONES",
            vitalTrends = "TENDENCIAS DE SIGNOS VITALES",
            symptomFrequency = "FRECUENCIA DE S\u00CDNTOMAS",
            weightLabel = "Peso",
            heightLabel = "Altura",
            knownConditions = "Condiciones conocidas",
            intensity = "intensidad",
            triggers = "Desencadenantes",
            notes = "Notas",
            trend = "Tendencia",
            trendUp = "en aumento",
            trendDown = "en descenso",
            trendStable = "estable",
            minValue = "M\u00EDn",
            maxValue = "M\u00E1x",
            avgValue = "Prom",
            avgIntensity = "Intensidad prom",
            maxIntensity = "Intensidad m\u00E1x",
            commonTriggers = "Desencadenantes comunes",
            abnormalReadings = "Lecturas anormales",
            period = "Per\u00EDodo",
            diastolicLabel = "Diast\u00F3lica",
            noSymptoms = "No hay registros de s\u00EDntomas para este per\u00EDodo.",
            noVitals = "No hay registros de signos vitales para este per\u00EDodo.",
            noMedications = "No hay medicamentos activos.",
            noPatterns = "No hay suficientes datos para el an\u00E1lisis de patrones.",
            highBP = "elevada (hipertensi\u00F3n)",
            lowBP = "baja (hipotensi\u00F3n)",
            highDiastolic = "diast\u00F3lica elevada",
            lowDiastolic = "diast\u00F3lica baja",
            highPulse = "elevado (taquicardia)",
            lowPulse = "bajo (bradicardia)",
            fever = "fiebre",
            subfebrile = "ligeramente elevada",
            hypothermia = "temperatura baja",
            criticalO2 = "ox\u00EDgeno cr\u00EDticamente bajo",
            lowO2 = "saturaci\u00F3n de ox\u00EDgeno baja",
            highGlucose = "glucosa significativamente elevada",
            elevatedGlucose = "glucosa elevada",
            lowGlucose = "glucosa baja (hipoglucemia)",
            severeSymptoms = "S\u00EDntomas severos (7+/10)",
            recurringSymptom = "S\u00EDntoma recurrente",
            sameDayCorrelation = "Correlaci\u00F3n del mismo d\u00EDa",
            diabetesGlucoseWarning = "Nota: Lecturas de glucosa elevadas con diabetes conocida \u2014 se recomienda consultar con su m\u00E9dico.",
            hypertensionBPWarning = "Nota: Presi\u00F3n arterial elevada con hipertensi\u00F3n conocida \u2014 se recomienda consultar con su m\u00E9dico.",
            disclaimer = "Este an\u00E1lisis fue generado localmente sin IA. Para un an\u00E1lisis m\u00E1s detallado, configure un proveedor de IA en la nube en Ajustes > IA."
        )

        val ZH_CN = AnalysisTexts(
            summaryTitle = "[\u5065\u5EB7\u6570\u636E\u6458\u8981]",
            patternTitle = "[\u6A21\u5F0F\u5206\u6790]",
            localAnalysisNote = "\u7531\u672C\u5730\u5206\u6790\u5F15\u64CE\u751F\u6210\uFF08\u8BE5\u8BBE\u5907\u4E0A\u65E0\u53EFAI SDK\uFF09\u3002",
            patientInfo = "\u60A3\u8005\u4FE1\u606F",
            symptomsSection = "\u75C7\u72B6",
            vitalsSection = "\u751F\u547D\u4F53\u5F81",
            medicationsSection = "\u836F\u7269",
            observationsSection = "\u89C2\u5BDF\u7ED3\u679C",
            correlationsSection = "\u76F8\u5173\u6027",
            vitalTrends = "\u751F\u547D\u4F53\u5F81\u8D8B\u52BF",
            symptomFrequency = "\u75C7\u72B6\u9891\u7387",
            weightLabel = "\u4F53\u91CD",
            heightLabel = "\u8EAB\u9AD8",
            knownConditions = "\u5DF2\u77E5\u75BE\u75C5",
            intensity = "\u5F3A\u5EA6",
            triggers = "\u8BF1\u56E0",
            notes = "\u5907\u6CE8",
            trend = "\u8D8B\u52BF",
            trendUp = "\u4E0A\u5347",
            trendDown = "\u4E0B\u964D",
            trendStable = "\u7A33\u5B9A",
            minValue = "\u6700\u5C0F",
            maxValue = "\u6700\u5927",
            avgValue = "\u5E73\u5747",
            avgIntensity = "\u5E73\u5747\u5F3A\u5EA6",
            maxIntensity = "\u6700\u5927\u5F3A\u5EA6",
            commonTriggers = "\u5E38\u89C1\u8BF1\u56E0",
            abnormalReadings = "\u5F02\u5E38\u8BFB\u6570",
            period = "\u65F6\u95F4\u6BB5",
            diastolicLabel = "\u8212\u5F20\u538B",
            noSymptoms = "\u8BE5\u671F\u95F4\u65E0\u75C7\u72B6\u8BB0\u5F55\u3002",
            noVitals = "\u8BE5\u671F\u95F4\u65E0\u751F\u547D\u4F53\u5F81\u8BB0\u5F55\u3002",
            noMedications = "\u65E0\u6B63\u5728\u4F7F\u7528\u7684\u836F\u7269\u3002",
            noPatterns = "\u6570\u636E\u4E0D\u8DB3\uFF0C\u65E0\u6CD5\u8FDB\u884C\u6A21\u5F0F\u5206\u6790\u3002",
            highBP = "\u5347\u9AD8\uFF08\u9AD8\u8840\u538B\uFF09",
            lowBP = "\u504F\u4F4E\uFF08\u4F4E\u8840\u538B\uFF09",
            highDiastolic = "\u8212\u5F20\u538B\u5347\u9AD8",
            lowDiastolic = "\u8212\u5F20\u538B\u504F\u4F4E",
            highPulse = "\u5347\u9AD8\uFF08\u5FC3\u52A8\u8FC7\u901F\uFF09",
            lowPulse = "\u504F\u4F4E\uFF08\u5FC3\u52A8\u8FC7\u7F13\uFF09",
            fever = "\u53D1\u70ED",
            subfebrile = "\u8F7B\u5FAE\u5347\u9AD8",
            hypothermia = "\u4F53\u6E29\u504F\u4F4E",
            criticalO2 = "\u6C27\u6C14\u4E25\u91CD\u4E0D\u8DB3",
            lowO2 = "\u8840\u6C27\u9971\u548C\u5EA6\u504F\u4F4E",
            highGlucose = "\u8840\u7CD6\u663E\u8457\u5347\u9AD8",
            elevatedGlucose = "\u8840\u7CD6\u5347\u9AD8",
            lowGlucose = "\u8840\u7CD6\u504F\u4F4E\uFF08\u4F4E\u8840\u7CD6\uFF09",
            severeSymptoms = "\u4E25\u91CD\u75C7\u72B6 (7+/10)",
            recurringSymptom = "\u53CD\u590D\u51FA\u73B0\u7684\u75C7\u72B6",
            sameDayCorrelation = "\u540C\u65E5\u76F8\u5173\u6027",
            diabetesGlucoseWarning = "\u6CE8\u610F\uFF1A\u5DF2\u77E5\u7CD6\u5C3F\u75C5\u60A3\u8005\u8840\u7CD6\u5347\u9AD8 \u2014 \u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u3002",
            hypertensionBPWarning = "\u6CE8\u610F\uFF1A\u5DF2\u77E5\u9AD8\u8840\u538B\u60A3\u8005\u8840\u538B\u5347\u9AD8 \u2014 \u5EFA\u8BAE\u4E0E\u533B\u751F\u8BA8\u8BBA\u3002",
            disclaimer = "\u6B64\u5206\u6790\u7531\u672C\u5730\u5F15\u64CE\u751F\u6210\uFF0C\u672A\u4F7F\u7528AI\u3002\u5982\u9700\u66F4\u8BE6\u7EC6\u7684\u5206\u6790\uFF0C\u8BF7\u5728\u8BBE\u7F6E > AI\u8BBE\u7F6E\u4E2D\u914D\u7F6E\u4E91AI\u63D0\u4F9B\u5546\u3002"
        )

        fun forLanguage(lang: String): AnalysisTexts = when (lang) {
            "en" -> EN
            "es" -> ES
            "zh-CN" -> ZH_CN
            else -> RU
        }
    }
}
