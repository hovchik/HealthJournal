package com.healthjournal.data.ai

import android.net.Uri
import com.healthjournal.R
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.LocalInferenceEngine
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class LocalAiProviderImpl(
    private val engine: LocalInferenceEngine
) : AiProvider {

    override val id = AiProviderId.LOCAL
    override val displayNameResId = R.string.ai_provider_local

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val lc = config.localAiConfig
        ensureModelLoaded(lc)
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = engine.generate(fullPrompt, GenerationParams(
            maxTokens = lc.maxTokens,
            temperature = lc.temperature
        ))
        return AiTextResult(text = result, providerId = id, modelUsed = lc.modelPath)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val lc = config.localAiConfig
        ensureModelLoaded(lc)
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val fullPrompt = "${prompt.system}\n\n${prompt.user}"
        val result = engine.generate(fullPrompt, GenerationParams(
            maxTokens = lc.maxTokens,
            temperature = lc.temperature
        ))
        return AiFlagsResult(text = result, providerId = id, modelUsed = lc.modelPath)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val lc = config.localAiConfig
        if (lc.modelPath.isBlank()) return ValidationResult(false, "Model path is required")
        if (lc.contextSize <= 0) return ValidationResult(false, "Context size must be positive")
        if (lc.maxTokens <= 0) return ValidationResult(false, "Max tokens must be positive")
        return ValidationResult(true)
    }

    override fun isOnlineRequired(): Boolean = false

    private suspend fun ensureModelLoaded(config: LocalAiConfig) {
        if (!engine.isModelLoaded()) {
            engine.loadModel(
                Uri.parse(config.modelPath),
                LocalModelParams(
                    contextSize = config.contextSize,
                    maxTokens = config.maxTokens,
                    temperature = config.temperature
                )
            )
        }
    }
}
