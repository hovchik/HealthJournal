package com.hovchik.healthjournal.domain.ai

import com.hovchik.healthjournal.data.ai.AiPreferences
import com.hovchik.healthjournal.data.ai.provider.AiProviderSelector
import com.hovchik.healthjournal.domain.model.ai.*
import com.hovchik.healthjournal.util.PrivacyRedactor

/**
 * Orchestrates AI provider selection, privacy redaction, and report generation.
 * Uses AiProviderSelector for mode-aware provider resolution.
 */
class AiService(
    private val registry: AiProviderRegistry,
    private val redactor: PrivacyRedactor,
    private val aiPreferences: AiPreferences,
    private val providerSelector: AiProviderSelector
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
            AiExecutionMode.CUSTOM_LOCAL -> {
                // Use custom local model provider (downloaded models)
                registry.getByIdOrDefault(AiProviderId.LOCAL)
            }
            AiExecutionMode.SYSTEM_LOCAL -> {
                // Use system AI provider via AiProviderSelector (not in registry)
                providerSelector.selectProvider().provider
            }
            AiExecutionMode.AUTO -> {
                // In auto mode: if user explicitly set a cloud provider, use it;
                // otherwise let the selector pick the best available
                val id = AiProviderId.fromKey(settings.selectedProviderId)
                if (id != AiProviderId.LOCAL) {
                    registry.getByIdOrDefault(id)
                } else {
                    providerSelector.selectProvider().provider
                }
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

    suspend fun generateResponse(prompt: String, settings: AiSettings? = null, providerId: AiProviderId? = null): String {
        val effectiveSettings = settings ?: AiSettings()
        val provider = if (providerId != null && providerId != AiProviderId.LOCAL) {
            registry.getByIdOrDefault(providerId)
        } else {
            getActiveProvider(effectiveSettings)
        }
        return provider.generateChatResponse(prompt, effectiveSettings).ifBlank { "No response generated" }
    }

    fun getAllProviders(): List<AiProvider> = registry.getAll()
}
