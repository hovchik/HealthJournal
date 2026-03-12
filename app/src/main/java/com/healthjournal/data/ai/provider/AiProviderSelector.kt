package com.healthjournal.data.ai.provider

import com.healthjournal.data.ai.AiPreferences
import com.healthjournal.data.ai.runtime.SystemAiRuntimeAdapter
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.model.ai.AiExecutionMode

data class ProviderSelection(
    val provider: AiProvider,
    val mode: AiExecutionMode,
    val reason: String
)

class AiProviderSelector(
    private val aiPreferences: AiPreferences,
    private val systemAiProvider: SystemAiProvider,
    private val customLocalProvider: CustomLocalModelProvider,
    private val cloudProviders: List<AiProvider>,
    private val systemRuntime: SystemAiRuntimeAdapter
) {
    suspend fun selectProvider(): ProviderSelection {
        val mode = aiPreferences.getSelectedMode()
        return when (mode) {
            AiExecutionMode.AUTO -> selectAuto()
            AiExecutionMode.SYSTEM_LOCAL -> selectSystemLocal()
            AiExecutionMode.CUSTOM_LOCAL -> selectCustomLocal()
            AiExecutionMode.CLOUD -> selectCloud()
        }
    }

    private fun selectAuto(): ProviderSelection {
        // Priority: System AI → Custom Local → Cloud
        if (systemRuntime.isAvailable()) {
            return ProviderSelection(systemAiProvider, AiExecutionMode.SYSTEM_LOCAL, "System AI is available")
        }
        if (customLocalProvider.validateConfig(com.healthjournal.domain.model.ai.AiSettings()).valid) {
            return ProviderSelection(customLocalProvider, AiExecutionMode.CUSTOM_LOCAL, "Local model is available")
        }
        val cloud = cloudProviders.firstOrNull()
        if (cloud != null) {
            return ProviderSelection(cloud, AiExecutionMode.CLOUD, "Using cloud AI")
        }
        return ProviderSelection(customLocalProvider, AiExecutionMode.CUSTOM_LOCAL, "Falling back to local analysis")
    }

    private fun selectSystemLocal(): ProviderSelection {
        if (systemRuntime.isAvailable()) {
            return ProviderSelection(systemAiProvider, AiExecutionMode.SYSTEM_LOCAL, "System AI selected")
        }
        // Fallback to custom local
        return ProviderSelection(customLocalProvider, AiExecutionMode.CUSTOM_LOCAL, "System AI unavailable, using local model")
    }

    private fun selectCustomLocal(): ProviderSelection {
        return ProviderSelection(customLocalProvider, AiExecutionMode.CUSTOM_LOCAL, "Local model selected")
    }

    private fun selectCloud(): ProviderSelection {
        val cloud = cloudProviders.firstOrNull()
            ?: return ProviderSelection(customLocalProvider, AiExecutionMode.CUSTOM_LOCAL, "No cloud provider, using local")
        return ProviderSelection(cloud, AiExecutionMode.CLOUD, "Cloud AI selected")
    }
}
