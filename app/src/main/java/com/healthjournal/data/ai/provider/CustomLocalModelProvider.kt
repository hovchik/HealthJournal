package com.healthjournal.data.ai.provider

import com.healthjournal.R
import com.healthjournal.data.ai.LocalModelManager
import com.healthjournal.data.ai.runtime.LiteRtRuntimeAdapter
import com.healthjournal.data.ai.runtime.LocalModelRuntime
import com.healthjournal.data.ai.runtime.MediaPipeLlmRuntimeAdapter
import com.healthjournal.data.ai.LocalAnalysisEngine
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class CustomLocalModelProvider(
    private val modelManager: LocalModelManager,
    private val mediaPipeRuntime: MediaPipeLlmRuntimeAdapter,
    private val liteRtRuntime: LiteRtRuntimeAdapter
) : AiProvider {

    override val id = AiProviderId.LOCAL
    override val displayNameResId = R.string.ai_provider_local

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val model = modelManager.getActiveModel()
        if (model == null) {
            return AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val runtime = resolveRuntime(model)
            val prompt = PromptTemplate.buildSummaryPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = runtime.runPrompt(fullPrompt)
            AiTextResult(text = result, providerId = id, modelUsed = model.displayName)
        } catch (_: Exception) {
            AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val model = modelManager.getActiveModel()
        if (model == null) {
            return AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }

        return try {
            val runtime = resolveRuntime(model)
            val prompt = PromptTemplate.buildPatternPrompt(input)
            val fullPrompt = "${prompt.system}\n\n${prompt.user}"
            val result = runtime.runPrompt(fullPrompt)
            AiFlagsResult(text = result, providerId = id, modelUsed = model.displayName)
        } catch (_: Exception) {
            AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val model = modelManager.getActiveModelSync()
        return if (model != null) {
            ValidationResult(true, "Active model: ${model.displayName}")
        } else {
            ValidationResult(true, "No model downloaded — using rule-based analysis")
        }
    }

    override fun isOnlineRequired(): Boolean = false

    private suspend fun resolveRuntime(model: LocalAiModel): LocalModelRuntime {
        val path = model.localPath ?: throw IllegalStateException("Model has no local path")
        return when (model.runtimeType) {
            "mediapipe_llm" -> {
                if (!mediaPipeRuntime.isAvailable()) {
                    mediaPipeRuntime.loadModel(path)
                }
                mediaPipeRuntime
            }
            "litert" -> {
                if (!liteRtRuntime.isAvailable()) {
                    liteRtRuntime.loadModel(path)
                }
                liteRtRuntime
            }
            else -> throw IllegalArgumentException("Unknown runtime: ${model.runtimeType}")
        }
    }
}
