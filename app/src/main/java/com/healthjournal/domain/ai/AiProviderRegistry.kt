package com.healthjournal.domain.ai

import com.healthjournal.domain.model.ai.AiProviderId

/**
 * Registry that holds all available AI providers and resolves them by ID.
 */
class AiProviderRegistry(
    private val providers: List<AiProvider>
) {
    fun getAll(): List<AiProvider> = providers

    fun getById(id: AiProviderId): AiProvider? =
        providers.firstOrNull { it.id == id }

    fun getByIdOrDefault(id: AiProviderId): AiProvider =
        getById(id) ?: providers.first()
}
