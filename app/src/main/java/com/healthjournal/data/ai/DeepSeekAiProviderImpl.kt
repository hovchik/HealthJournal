package com.healthjournal.data.ai

import com.healthjournal.R
import com.healthjournal.data.remote.openai.OpenAiApi
import com.healthjournal.data.remote.openai.OpenAiMessage
import com.healthjournal.data.remote.openai.OpenAiRequest
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class DeepSeekAiProviderImpl(
    private val apiFactory: (config: DeepSeekConfig) -> OpenAiApi
) : AiProvider {

    override val id = AiProviderId.DEEPSEEK
    override val displayNameResId = R.string.ai_provider_deepseek

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val api = apiFactory(config.deepSeekConfig)
        val request = OpenAiRequest(
            model = config.deepSeekConfig.model,
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.system),
                OpenAiMessage(role = "user", content = prompt.user)
            )
        )
        val response = api.chatCompletion(request)
        val text = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from DeepSeek")
        return AiTextResult(text = text, providerId = id, modelUsed = config.deepSeekConfig.model)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val api = apiFactory(config.deepSeekConfig)
        val request = OpenAiRequest(
            model = config.deepSeekConfig.model,
            messages = listOf(
                OpenAiMessage(role = "system", content = prompt.system),
                OpenAiMessage(role = "user", content = prompt.user)
            )
        )
        val response = api.chatCompletion(request)
        val text = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from DeepSeek")
        return AiFlagsResult(text = text, providerId = id, modelUsed = config.deepSeekConfig.model)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val c = config.deepSeekConfig
        if (c.apiKey.isBlank()) return ValidationResult(false, "DeepSeek API key is required")
        if (c.model.isBlank()) return ValidationResult(false, "Model name is required")
        if (c.baseUrl.isBlank()) return ValidationResult(false, "Base URL is required")
        return ValidationResult(true)
    }

    override fun isOnlineRequired(): Boolean = true
}
