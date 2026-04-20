package com.hovchik.healthjournal.domain.ai

import androidx.annotation.StringRes
import com.hovchik.healthjournal.domain.model.ai.*

/**
 * Provider abstraction for AI backends.
 * Each provider implements this interface to generate summaries and pattern analyses.
 */
interface AiProvider {
    val id: AiProviderId

    @get:StringRes
    val displayNameResId: Int

    suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult

    suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult

    fun validateConfig(config: AiSettings): ValidationResult

    fun isOnlineRequired(): Boolean

    /**
     * Generates a chat response from a raw user prompt.
     * Default implementation delegates to generateDoctorSummary with the prompt as user content.
     */
    suspend fun generateChatResponse(prompt: String, config: AiSettings): String {
        val input = AiInput(
            symptoms = emptyList(), vitals = emptyList(), medications = emptyList(),
            periodDays = 7, outputLanguage = "en"
        )
        return generateDoctorSummary(input, config).text
    }
}
