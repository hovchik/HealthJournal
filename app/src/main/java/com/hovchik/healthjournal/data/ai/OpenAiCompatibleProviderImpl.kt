package com.hovchik.healthjournal.data.ai

import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.data.remote.openai.OpenAiApi
import com.hovchik.healthjournal.data.remote.openai.OpenAiMessage
import com.hovchik.healthjournal.data.remote.openai.OpenAiRequest
import com.hovchik.healthjournal.domain.ai.AiProvider
import com.hovchik.healthjournal.domain.ai.PromptTemplate
import com.hovchik.healthjournal.domain.model.ai.*

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

    override suspend fun generateChatResponse(prompt: String, config: AiSettings): String {
        val api = apiFactory(config.openAiConfig)
        val request = OpenAiRequest(
            model = config.openAiConfig.model,
            messages = listOf(
                OpenAiMessage(role = "system", content = "You are a helpful health assistant. Answer the user's health-related questions based on the provided context. Be concise and informative."),
                OpenAiMessage(role = "user", content = prompt)
            )
        )
        val response = api.chatCompletion(request)
        return response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from OpenAI-compatible API")
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
