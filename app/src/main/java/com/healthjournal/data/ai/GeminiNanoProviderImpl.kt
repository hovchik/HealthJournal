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
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

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
            AiTextResult(text = LocalAnalysisEngine.buildLocalSummary(input), providerId = id, modelUsed = "local-analysis")
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = tryRunInference(fullPrompt, config.geminiNanoConfig)
        return if (result != null) {
            AiFlagsResult(text = result.first, providerId = id, modelUsed = result.second)
        } else {
            AiFlagsResult(text = LocalAnalysisEngine.buildLocalPatternAnalysis(input), providerId = id, modelUsed = "local-analysis")
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
}

