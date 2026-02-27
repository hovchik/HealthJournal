package com.healthjournal.data.ai

import com.healthjournal.R
import com.healthjournal.data.remote.openai.OpenAiApi
import com.healthjournal.data.remote.openai.OpenAiMessage
import com.healthjournal.data.remote.openai.OpenAiRequest
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class OpenAiCompatibleProviderImpl(
    private val apiFactory: (config: OpenAiConfig) -> OpenAiApi
) : AiProvider {

    override val id = AiProviderId.OPENAI_COMPATIBLE
    override val displayNameResId = R.string.ai_provider_openai

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val api = apiFactory(config.openAiConfig)
        val request = OpenAiRequest(
            model = config.openAiConfig.model,
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.system),
                OpenAiMessage(role = "user", content = prompt.user)
            )
        )
        val response = api.chatCompletion(request)
        val text = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from OpenAI-compatible API")
        return AiTextResult(text = text, providerId = id, modelUsed = config.openAiConfig.model)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val api = apiFactory(config.openAiConfig)
        val request = OpenAiRequest(
            model = config.openAiConfig.model,
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.system),
                OpenAiMessage(role = "user", content = prompt.user)
            )
        )
        val response = api.chatCompletion(request)
        val text = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from OpenAI-compatible API")
        return AiFlagsResult(text = text, providerId = id, modelUsed = config.openAiConfig.model)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val c = config.openAiConfig
        if (c.apiKey.isBlank()) return ValidationResult(false, "API key is required")
        if (c.model.isBlank()) return ValidationResult(false, "Model name is required")
        if (c.baseUrl.isBlank()) return ValidationResult(false, "Base URL is required")
        return ValidationResult(true)
    }

    override fun isOnlineRequired(): Boolean = true
}
