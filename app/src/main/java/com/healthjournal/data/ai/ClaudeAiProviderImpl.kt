package com.healthjournal.data.ai

import com.healthjournal.R
import com.healthjournal.data.remote.api.ClaudeApi
import com.healthjournal.data.remote.dto.ClaudeMessage
import com.healthjournal.data.remote.dto.ClaudeRequest
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class ClaudeAiProviderImpl(
    private val apiFactory: (config: ClaudeConfig) -> ClaudeApi
) : AiProvider {

    override val id = AiProviderId.CLAUDE
    override val displayNameResId = R.string.ai_provider_claude

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val api = apiFactory(config.claudeConfig)
        val request = ClaudeRequest(
            model = config.claudeConfig.model,
            maxTokens = 2048,
            system = prompt.system,
            messages = listOf(ClaudeMessage(role = "user", content = prompt.user))
        )
        val response = api.sendMessage(request)
        val text = response.content.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from Claude")
        return AiTextResult(text = text, providerId = id, modelUsed = config.claudeConfig.model)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val api = apiFactory(config.claudeConfig)
        val request = ClaudeRequest(
            model = config.claudeConfig.model,
            maxTokens = 2048,
            system = prompt.system,
            messages = listOf(ClaudeMessage(role = "user", content = prompt.user))
        )
        val response = api.sendMessage(request)
        val text = response.content.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from Claude")
        return AiFlagsResult(text = text, providerId = id, modelUsed = config.claudeConfig.model)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val c = config.claudeConfig
        if (c.apiKey.isBlank()) return ValidationResult(false, "Claude API key is required")
        if (c.model.isBlank()) return ValidationResult(false, "Model name is required")
        if (c.baseUrl.isBlank()) return ValidationResult(false, "Base URL is required")
        return ValidationResult(true)
    }

    override fun isOnlineRequired(): Boolean = true
}
