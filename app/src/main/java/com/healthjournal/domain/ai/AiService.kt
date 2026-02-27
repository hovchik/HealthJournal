package com.healthjournal.domain.ai

import com.healthjournal.domain.model.ai.*
import com.healthjournal.util.PrivacyRedactor

/**
 * Orchestrates AI provider selection, privacy redaction, and report generation.
 */
class AiService(
    private val registry: AiProviderRegistry,
    private val redactor: PrivacyRedactor
) {
    fun getActiveProvider(settings: AiSettings): AiProvider {
        val id = AiProviderId.fromKey(settings.selectedProviderId)
        return registry.getByIdOrDefault(id)
    }

    fun validateActiveProvider(settings: AiSettings): ValidationResult {
        val provider = getActiveProvider(settings)
        return provider.validateConfig(settings)
    }

    suspend fun generateDoctorSummary(input: AiInput, settings: AiSettings): AiTextResult {
        val provider = getActiveProvider(settings)
        val sanitizedInput = if (settings.privacyRedactEnabled) redactor.redactInput(input) else input
        return provider.generateDoctorSummary(sanitizedInput, settings)
    }

    suspend fun analyzePatterns(input: AiInput, settings: AiSettings): AiFlagsResult {
        val provider = getActiveProvider(settings)
        val sanitizedInput = if (settings.privacyRedactEnabled) redactor.redactInput(input) else input
        return provider.analyzePatterns(sanitizedInput, settings)
    }

    fun getAllProviders(): List<AiProvider> = registry.getAll()
}
