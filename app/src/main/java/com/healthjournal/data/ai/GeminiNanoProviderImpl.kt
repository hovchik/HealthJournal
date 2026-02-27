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
 * On-device AI provider using Google AI Edge SDK (Gemini Nano).
 * Requires Android 14+ (API 34) with Google Play Services AI Core.
 * Falls back to a local data summary if Gemini Nano is not available.
 */
class GeminiNanoProviderImpl(
    private val context: Context
) : AiProvider {

    override val id = AiProviderId.GEMINI_NANO
    override val displayNameResId = R.string.ai_provider_gemini_nano

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = runInference(fullPrompt, config.geminiNanoConfig)
        return AiTextResult(text = result, providerId = id, modelUsed = "gemini-nano")
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = runInference(fullPrompt, config.geminiNanoConfig)
        return AiFlagsResult(text = result, providerId = id, modelUsed = "gemini-nano")
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        if (Build.VERSION.SDK_INT < 34) {
            return ValidationResult(false,
                "Gemini Nano requires Android 14 (API 34) or higher. " +
                "Current device is API ${Build.VERSION.SDK_INT}.")
        }
        return if (isGeminiNanoAvailable()) {
            ValidationResult(true)
        } else {
            ValidationResult(false,
                "Gemini Nano is not available on this device. " +
                "Requires Android 14+ with Google Play Services AI Core " +
                "and a supported device (e.g., Pixel 8 Pro, Galaxy S24).")
        }
    }

    override fun isOnlineRequired(): Boolean = false

    private suspend fun runInference(prompt: String, config: GeminiNanoConfig): String =
        withContext(Dispatchers.IO) {
            // Try Google AI Edge SDK (aicore)
            try {
                return@withContext runViaAiCore(prompt, config)
            } catch (_: Exception) { }

            // Try ML Kit GenAI
            try {
                return@withContext runViaMlKit(prompt, config)
            } catch (_: Exception) { }

            // Fallback: local data extraction
            buildFallbackResponse(prompt)
        }

    /**
     * Run inference via com.google.ai.edge.aicore.GenerativeModel.
     * Uses reflection to avoid compile-time hard dependency,
     * allowing the app to build even when the SDK is absent.
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
        return text as? String ?: "No response from Gemini Nano"
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
        return text as? String ?: "No response from ML Kit GenAI"
    }

    private fun isGeminiNanoAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return false
        return try {
            Class.forName("com.google.ai.edge.aicore.GenerativeModel")
            true
        } catch (_: ClassNotFoundException) {
            try {
                Class.forName("com.google.mlkit.genai.inference.GenerativeModel")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
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

        return buildString {
            appendLine("[On-Device Analysis - Gemini Nano]")
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
            if (Build.VERSION.SDK_INT < 34) {
                appendLine("Note: Gemini Nano requires Android 14 (API 34) or higher.")
                appendLine("Your device is running API ${Build.VERSION.SDK_INT}.")
            } else {
                appendLine("Note: Gemini Nano AI Core is not installed on this device.")
                appendLine("Ensure Google Play Services is updated and AI Core is available.")
            }
            appendLine("You can also configure a cloud AI provider in Settings > AI Settings.")
        }
    }
}
