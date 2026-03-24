package com.healthjournal.data.ai

import com.healthjournal.R
import com.healthjournal.data.remote.gemini.GeminiApi
import com.healthjournal.data.remote.gemini.GeminiContent
import com.healthjournal.data.remote.gemini.GeminiPart
import com.healthjournal.data.remote.gemini.GeminiRequest
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.ai.PromptTemplate
import com.healthjournal.domain.model.ai.*

class GeminiAiProviderImpl(
    private val apiFactory: (config: GeminiConfig) -> GeminiApi
) : AiProvider {

    override val id = AiProviderId.GEMINI
    override val displayNameResId = R.string.ai_provider_gemini

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        val api = apiFactory(config.geminiConfig)
        val request = GeminiRequest(
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = prompt.system))
            ),
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt.user))
                )
            )
        )
        val response = api.generateContent(
            model = config.geminiConfig.model,
            apiKey = config.geminiConfig.apiKey,
            request = request
        )
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from Gemini")
        return AiTextResult(text = text, providerId = id, modelUsed = config.geminiConfig.model)
    }

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
        val prompt = PromptTemplate.buildPatternPrompt(input)
        val api = apiFactory(config.geminiConfig)
        val request = GeminiRequest(
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = prompt.system))
            ),
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt.user))
                )
            )
        )
        val response = api.generateContent(
            model = config.geminiConfig.model,
            apiKey = config.geminiConfig.apiKey,
            request = request
        )
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from Gemini")
        return AiFlagsResult(text = text, providerId = id, modelUsed = config.geminiConfig.model)
    }

    override fun validateConfig(config: AiSettings): ValidationResult {
        val c = config.geminiConfig
        if (c.apiKey.isBlank()) return ValidationResult(false, "Gemini API key is required")
        if (c.model.isBlank()) return ValidationResult(false, "Model name is required")
        if (c.baseUrl.isBlank()) return ValidationResult(false, "Base URL is required")
        return ValidationResult(true)
    }

    override fun isOnlineRequired(): Boolean = true
}
