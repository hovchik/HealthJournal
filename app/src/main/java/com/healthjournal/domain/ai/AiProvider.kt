package com.healthjournal.domain.ai

import androidx.annotation.StringRes
import com.healthjournal.domain.model.ai.*

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
}
