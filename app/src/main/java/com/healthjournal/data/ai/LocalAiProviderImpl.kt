package com.healthjournal.data.ai

import android.content.Context
import com.healthjournal.R
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

/**
 * On-device AI provider that uses a downloaded LLM model via MediaPipe LLM Inference.
 * Falls back to LocalAnalysisEngine (rule-based) when no model is downloaded or loaded.
 */
class LocalAiProviderImpl(
    private val engine: MediaPipeLlmEngine,
    private val modelRepository: ModelRepository,
    private val context: Context
) : AiProvider {

    override val id = AiProviderId.LOCAL
    override val displayNameResId = R.string.ai_provider_local

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val modelPath = modelRepository.getDownloadedModelPath()

        if (modelPath == null) {
            return AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val lc = config.localAiConfig
            ensureModelLoaded(modelPath, lc)
            val prompt = PromptTemplate.buildSummaryPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = engine.generate(fullPrompt, GenerationParams(
                maxTokens = lc.maxTokens,
                temperature = lc.temperature
            ))
            val modelName = modelRepository.getDownloadedModel()?.name ?: "on-device LLM"
            AiTextResult(text = result, providerId = id, modelUsed = modelName)
        } catch (_: Exception) {
            AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val modelPath = modelRepository.getDownloadedModelPath()

        if (modelPath == null) {
            return AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val lc = config.localAiConfig
            ensureModelLoaded(modelPath, lc)
            val prompt = PromptTemplate.buildPatternPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = engine.generate(fullPrompt, GenerationParams(
                maxTokens = lc.maxTokens,
                temperature = lc.temperature
            ))
            val modelName = modelRepository.getDownloadedModel()?.name ?: "on-device LLM"
            AiFlagsResult(text = result, providerId = id, modelUsed = modelName)
        } catch (_: Exception) {
            AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val hasModel = modelRepository.getDownloadedModelPath() != null
        return if (hasModel) {
            ValidationResult(true, "Model downloaded and ready")
        } else {
            ValidationResult(true, "No model downloaded — using rule-based analysis")
        }
    }

    override fun isOnlineRequired(): Boolean = false

    private suspend fun ensureModelLoaded(modelPath: String, config: LocalAiConfig) {
        if (!engine.isModelLoaded()) {
            engine.loadModelFromPath(
                modelPath,
                LocalModelParams(
                    contextSize = config.contextSize,
                    maxTokens = config.maxTokens,
                    temperature = config.temperature
                ),
                context
            )
        }
    }
}
