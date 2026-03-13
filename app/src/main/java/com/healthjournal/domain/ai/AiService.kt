package com.healthjournal.domain.ai

import com.healthjournal.data.ai.AiPreferences
import com.healthjournal.domain.model.ai.*
import com.healthjournal.util.PrivacyRedactor

/**
 * Orchestrates AI provider selection, privacy redaction, and report generation.
 */
class AiService(
    private val registry: AiProviderRegistry,
    private val redactor: PrivacyRedactor,
    private val aiPreferences: AiPreferences
) {
    suspend fun getActiveProvider(settings: AiSettings): AiProvider {
        val mode = aiPreferences.getSelectedMode()
        return when (mode) {
            AiExecutionMode.CLOUD -> {
                // Use the specific cloud provider selected in settings
                val id = AiProviderId.fromKey(settings.selectedProviderId)
                if (id != AiProviderId.LOCAL) {
                    registry.getByIdOrDefault(id)
                } else {
                    // User is in cloud mode but selectedProviderId is still "local" — pick first cloud provider
                    registry.getAll().firstOrNull { it.isOnlineRequired() }
                        ?: registry.getByIdOrDefault(AiProviderId.CLAUDE)
                }
            }
            AiExecutionMode.CUSTOM_LOCAL, AiExecutionMode.SYSTEM_LOCAL -> {
                registry.getByIdOrDefault(AiProviderId.LOCAL)
            }
            AiExecutionMode.AUTO -> {
                // Respect selectedProviderId if set to a cloud provider, otherwise use local
                val id = AiProviderId.fromKey(settings.selectedProviderId)
                registry.getByIdOrDefault(id)
            }
        }
    }

    fun validateActiveProvider(settings: AiSettings): ValidationResult {
        val id = AiProviderId.fromKey(settings.selectedProviderId)
        val provider = registry.getByIdOrDefault(id)
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
