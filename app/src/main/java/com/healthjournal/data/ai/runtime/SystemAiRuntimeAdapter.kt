package com.healthjournal.data.ai.runtime

import android.content.Context
import android.content.pm.PackageManager

class SystemAiRuntimeAdapter(private val context: Context) : LocalModelRuntime {
    override val runtimeId = "system_aicore"
    override val displayName = "Android AICore (Gemini Nano)"

    override fun isAvailable(): Boolean = isAiCoreInstalled()

    override suspend fun runPrompt(prompt: String): String {
        if (!isAvailable()) throw IllegalStateException("AICore is not available")
        // AICore public API is not yet stable — placeholder for future integration
        // When stable, use: com.google.ai.edge.aicore.GenerativeModel
        throw UnsupportedOperationException(
            "System AI inference requires Android AICore. " +
            "The public API is not yet stable for direct integration. " +
            "Use the Google AI Edge Gallery app or wait for stable API release."
        )
    }

    override fun supportsStructuredJson(): Boolean = false
    override fun release() { /* AICore manages its own lifecycle */ }

    fun getStatusMessage(): String {
        val installed = isAiCoreInstalled()
        val version = getAiCoreVersion()
        return when {
            installed && version != null -> "AICore v$version installed"
            installed -> "AICore installed"
            else -> "AICore not found on this device"
        }
    }

    private fun isAiCoreInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.aicore", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) { false }
    }

    private fun getAiCoreVersion(): String? {
        return try {
            context.packageManager.getPackageInfo("com.google.android.aicore", 0).versionName
        } catch (_: PackageManager.NameNotFoundException) { null }
    }
}
