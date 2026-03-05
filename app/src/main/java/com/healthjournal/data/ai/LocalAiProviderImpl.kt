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
        // If engine is a stub, skip it and go straight to LocalAnalysisEngine
        if (engine is StubLocalInferenceEngine) {
            return AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val lc = config.localAiConfig
            ensureModelLoaded(lc)
            val prompt = PromptTemplate.buildSummaryPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = engine.generate(fullPrompt, GenerationParams(
                maxTokens = lc.maxTokens,
                temperature = lc.temperature
            ))
            AiTextResult(text = result, providerId = id, modelUsed = lc.modelPath)
        } catch (_: Exception) {
            // Fall back to local rule-based analysis
            AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        // If engine is a stub, skip it and go straight to LocalAnalysisEngine
        if (engine is StubLocalInferenceEngine) {
            return AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val lc = config.localAiConfig
            ensureModelLoaded(lc)
            val prompt = PromptTemplate.buildPatternPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = engine.generate(fullPrompt, GenerationParams(
                maxTokens = lc.maxTokens,
                temperature = lc.temperature
            ))
            AiFlagsResult(text = result, providerId = id, modelUsed = lc.modelPath)
        } catch (_: Exception) {
            // Fall back to local rule-based analysis
            AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        // Local analysis is always available via LocalAnalysisEngine
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
