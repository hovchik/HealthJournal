package com.healthjournal.data.ai.provider

import com.healthjournal.R
import com.healthjournal.data.ai.LocalAnalysisEngine
import com.healthjournal.data.ai.runtime.SystemAiRuntimeAdapter
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class SystemAiProvider(
    private val systemRuntime: SystemAiRuntimeAdapter
) : AiProvider {

    override val id = AiProviderId.LOCAL  // shares LOCAL id but wraps system AI
    override val displayNameResId = R.string.ai_provider_system_ai

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        if (!systemRuntime.isAvailable()) {
            // System AI not available — fall back to rule-based analysis
            return AiTextResult(
                text = LocalAnalysisEngine.buildLocalSummary(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val result = systemRuntime.runPrompt("${prompt.system}\n\n${prompt.user}")
        return AiTextResult(text = result, providerId = id, modelUsed = "Android AICore")
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        if (!systemRuntime.isAvailable()) {
            return AiFlagsResult(
                text = LocalAnalysisEngine.buildLocalPatternAnalysis(input),
                providerId = id,
                modelUsed = "local-analysis"
            )
        }
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val result = systemRuntime.runPrompt("${prompt.system}\n\n${prompt.user}")
        return AiFlagsResult(text = result, providerId = id, modelUsed = "Android AICore")
    }

    override suspend fun generateChatResponse(prompt: String, config: AiSettings): String {
        if (!systemRuntime.isAvailable()) {
            return "System AI is not available on this device. Please select a cloud AI provider."
        }
        return systemRuntime.runPrompt(prompt)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        return if (systemRuntime.isAvailable()) {
            ValidationResult(true, systemRuntime.getStatusMessage())
        } else {
            ValidationResult(false, "Android AICore is not installed on this device")
        }
    }

    override fun isOnlineRequired(): Boolean = false
}
