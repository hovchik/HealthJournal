package com.healthjournal.data.ai

import android.content.Context
import android.os.Build
import com.healthjournal.R
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device AI provider using Google AI Edge SDK (Gemini Nano) or Samsung Galaxy AI.
 * Requires Android 14+ (API 34).
 * Priority: Google AI Edge → ML Kit GenAI → Samsung Galaxy AI → fallback summary.
 */
class GeminiNanoProviderImpl(
    private val context: Context
) : AiProvider {

    override val id = AiProviderId.GEMINI_NANO
    override val displayNameResId = R.string.ai_provider_gemini_nano

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val (result, model) = runInference(fullPrompt, config.geminiNanoConfig)
        return AiTextResult(text = result, providerId = id, modelUsed = model)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val (result, model) = runInference(fullPrompt, config.geminiNanoConfig)
        return AiFlagsResult(text = result, providerId = id, modelUsed = model)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        if (Build.VERSION.SDK_INT < 34) {
            return ValidationResult(false,
                "System AI requires Android 14 (API 34) or higher. " +
                "Current device is API ${Build.VERSION.SDK_INT}.")
        }
        val availability = checkAvailability()
        return if (availability != AvailabilityStatus.NONE) {
            ValidationResult(true, "Available: ${availability.label}")
        } else {
            ValidationResult(false,
                "On-device AI is not available. " +
                "Requires Google AI Core (Pixel 8+) or Samsung Galaxy AI (Galaxy S24+).")
        }
    }

    override fun isOnlineRequired(): Boolean = false

    /**
     * Runs inference through available on-device AI engines in priority order.
     * Returns a Pair of (responseText, modelName).
     */
    private suspend fun runInference(prompt: String, config: GeminiNanoConfig): Pair<String, String> =
        withContext(Dispatchers.IO) {
            // 1. Google AI Edge SDK (aicore)
            try {
                return@withContext runViaAiCore(prompt, config) to "gemini-nano"
            } catch (_: Exception) { }

            // 2. ML Kit GenAI
            try {
                return@withContext runViaMlKit(prompt, config) to "mlkit-genai"
            } catch (_: Exception) { }

            // 3. Samsung Galaxy AI – try multiple SDK paths
            try {
                return@withContext runViaSamsungAi(prompt, config) to "galaxy-ai"
            } catch (_: Exception) { }

            // 4. Fallback: local data extraction
            buildFallbackResponse(prompt) to "fallback"
        }

    /**
     * Run inference via com.google.ai.edge.aicore.GenerativeModel.
     */
    private suspend fun runViaAiCore(prompt: String, config: GeminiNanoConfig): String {
        val generationConfigClass = Class.forName("com.google.ai.edge.aicore.GenerationConfig")
        val builderClass = Class.forName("com.google.ai.edge.aicore.GenerationConfig\$Builder")
        val builder = builderClass.getDeclaredConstructor().newInstance()
        builderClass.getMethod("setTemperature", Float::class.java)
            .invoke(builder, config.temperature)
        builderClass.getMethod("setTopK", Int::class.java)
            .invoke(builder, config.topK)
        builderClass.getMethod("setMaxOutputTokens", Int::class.java)
            .invoke(builder, config.maxOutputTokens)
        val genConfig = builderClass.getMethod("build").invoke(builder)

        val modelClass = Class.forName("com.google.ai.edge.aicore.GenerativeModel")
        val model = modelClass.getConstructor(generationConfigClass).newInstance(genConfig)

        val response = modelClass.getMethod("generateContent", String::class.java)
            .invoke(model, prompt)
        val text = response.javaClass.getMethod("getText").invoke(response)
        return text as? String ?: throw RuntimeException("Empty response from AI Core")
    }

    /**
     * Fallback: try ML Kit GenAI inference API.
     */
    private suspend fun runViaMlKit(prompt: String, config: GeminiNanoConfig): String {
        val inferenceClass = Class.forName("com.google.mlkit.genai.inference.GenerativeModel")
        val configClass = Class.forName("com.google.mlkit.genai.inference.GenerationConfig")
        val configBuilder = configClass.getMethod("builder").invoke(null)
        val configBuilderClass = configBuilder.javaClass
        configBuilderClass.getMethod("setTemperature", Float::class.java)
            .invoke(configBuilder, config.temperature)
        configBuilderClass.getMethod("setTopK", Int::class.java)
            .invoke(configBuilder, config.topK)
        configBuilderClass.getMethod("setMaxOutputTokens", Int::class.java)
            .invoke(configBuilder, config.maxOutputTokens)
        val genConfig = configBuilderClass.getMethod("build").invoke(configBuilder)

        val model = inferenceClass.getMethod("create", configClass).invoke(null, genConfig)
        val response = inferenceClass.getMethod("generateContent", String::class.java)
            .invoke(model, prompt)
        val text = response.javaClass.getMethod("getText").invoke(response)
        return text as? String ?: throw RuntimeException("Empty response from ML Kit")
    }

    /**
     * Run inference via Samsung Galaxy AI.
     * Tries multiple Samsung SDK API paths, as the API surface varies
     * across One UI versions (Galaxy S24, S25, etc.).
     */
    private fun runViaSamsungAi(prompt: String, config: GeminiNanoConfig): String {
        // Path 1: Samsung AI Inference Engine (One UI 6.1+)
        try {
            val engineClass = Class.forName("com.samsung.android.sdk.aiinference.InferenceEngine")
            val engine = engineClass.getMethod("getInstance", Context::class.java)
                .invoke(null, context)
            val response = engineClass.getMethod("generateText", String::class.java)
                .invoke(engine, prompt)
            val result = response as? String
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) { }

        // Path 2: Samsung GenAI model API
        try {
            val modelClass = Class.forName("com.samsung.android.sdk.ai.model.GenAIModel")
            val getInstance = modelClass.getMethod("getInstance", Context::class.java)
            val model = getInstance.invoke(null, context)
            val response = modelClass.getMethod("generate", String::class.java)
                .invoke(model, prompt)
            val result = response as? String
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) { }

        // Path 3: Samsung AI Core on-device service via content provider
        try {
            val uri = android.net.Uri.parse("content://com.samsung.android.aicoreondevice/generate")
            val cursor = context.contentResolver.query(
                uri, null, prompt, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    if (!result.isNullOrBlank()) return result
                }
            }
        } catch (_: Exception) { }

        // Path 4: Samsung Galaxy AI app provider
        try {
            val uri = android.net.Uri.parse("content://com.samsung.android.galaxyai.provider/generate")
            val cursor = context.contentResolver.query(
                uri, null, prompt, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    if (!result.isNullOrBlank()) return result
                }
            }
        } catch (_: Exception) { }

        // Path 5: Samsung Intelligence service
        try {
            val uri = android.net.Uri.parse("content://com.samsung.android.intelligence/generate")
            val cursor = context.contentResolver.query(
                uri, null, prompt, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    if (!result.isNullOrBlank()) return result
                }
            }
        } catch (_: Exception) { }

        throw RuntimeException("No Samsung AI SDK path available")
    }

    private enum class AvailabilityStatus(val label: String) {
        GOOGLE_AI_CORE("Google AI Core (Gemini Nano)"),
        MLKIT_GENAI("ML Kit GenAI"),
        SAMSUNG_GALAXY_AI("Samsung Galaxy AI"),
        SAMSUNG_DEVICE("Samsung device (Galaxy AI expected)"),
        GOOGLE_DEVICE("Google device (Gemini Nano expected)"),
        NONE("Not available")
    }

    private fun checkAvailability(): AvailabilityStatus {
        if (Build.VERSION.SDK_INT < 34) return AvailabilityStatus.NONE

        // Check SDK classes
        try { Class.forName("com.google.ai.edge.aicore.GenerativeModel"); return AvailabilityStatus.GOOGLE_AI_CORE }
        catch (_: ClassNotFoundException) { }
        try { Class.forName("com.google.mlkit.genai.inference.GenerativeModel"); return AvailabilityStatus.MLKIT_GENAI }
        catch (_: ClassNotFoundException) { }
        try { Class.forName("com.samsung.android.sdk.aiinference.InferenceEngine"); return AvailabilityStatus.SAMSUNG_GALAXY_AI }
        catch (_: ClassNotFoundException) { }
        try { Class.forName("com.samsung.android.sdk.ai.model.GenAIModel"); return AvailabilityStatus.SAMSUNG_GALAXY_AI }
        catch (_: ClassNotFoundException) { }

        // Check installed AI packages
        val pm = context.packageManager
        val googlePackages = listOf("com.google.android.aicore")
        for (pkg in googlePackages) {
            try { pm.getPackageInfo(pkg, 0); return AvailabilityStatus.GOOGLE_AI_CORE }
            catch (_: Exception) { }
        }
        val samsungPackages = listOf(
            "com.samsung.android.aicoreondevice",
            "com.samsung.android.galaxyai",
            "com.samsung.android.intelligence"
        )
        for (pkg in samsungPackages) {
            try { pm.getPackageInfo(pkg, 0); return AvailabilityStatus.SAMSUNG_GALAXY_AI }
            catch (_: Exception) { }
        }

        // Known compatible manufacturers on API 34+
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer == "samsung") return AvailabilityStatus.SAMSUNG_DEVICE
        if (manufacturer == "google") return AvailabilityStatus.GOOGLE_DEVICE

        return AvailabilityStatus.NONE
    }

    private fun buildFallbackResponse(prompt: String): String {
        val lines = prompt.lines()
        val symptomLines = mutableListOf<String>()
        val vitalLines = mutableListOf<String>()
        var section = ""

        for (line in lines) {
            when {
                line.contains("СИМПТОМЫ") || line.contains("SYMPTOMS") ||
                    line.contains("症状") || line.contains("SINTOMAS") -> section = "symptoms"
                line.contains("ПОКАЗАТЕЛИ") || line.contains("VITALS") ||
                    line.contains("生命体征") || line.contains("SIGNOS") -> section = "vitals"
                line.contains("ЛЕКАРСТВА") || line.contains("MEDICATIONS") ||
                    line.contains("药物") || line.contains("MEDICAMENTOS") -> section = "meds"
                line.startsWith("- ") -> when (section) {
                    "symptoms" -> symptomLines.add(line)
                    "vitals" -> vitalLines.add(line)
                }
            }
        }

        val manufacturer = Build.MANUFACTURER.lowercase()
        val deviceHint = if (manufacturer == "samsung") {
            "Galaxy AI is not responding on this device.\n" +
            "Please ensure Galaxy AI is enabled in Settings > Galaxy AI,\n" +
            "and that the on-device AI model has been downloaded."
        } else if (Build.VERSION.SDK_INT < 34) {
            "System AI requires Android 14 (API 34) or higher.\n" +
            "Your device is running API ${Build.VERSION.SDK_INT}."
        } else {
            "On-device AI is not installed.\n" +
            "For Google devices, ensure Google Play Services is updated and AI Core is available.\n" +
            "For Samsung devices, ensure Galaxy AI is enabled."
        }

        return buildString {
            appendLine("[On-Device Data Summary]")
            appendLine()
            if (symptomLines.isNotEmpty()) {
                appendLine("Symptoms recorded: ${symptomLines.size}")
                symptomLines.forEach { appendLine(it) }
                appendLine()
            }
            if (vitalLines.isNotEmpty()) {
                appendLine("Vital signs recorded: ${vitalLines.size}")
                vitalLines.forEach { appendLine(it) }
                appendLine()
            }
            appendLine("Note: $deviceHint")
            appendLine("You can also configure a cloud AI provider in Settings > AI Settings.")
        }
    }
}
