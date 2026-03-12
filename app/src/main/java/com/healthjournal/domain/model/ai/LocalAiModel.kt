package com.healthjournal.domain.model.ai

import kotlinx.serialization.Serializable

data class LocalAiModel(
    val modelId: String,
    val displayName: String,
    val runtimeType: String,
    val fileFormat: String,
    val quantization: String?,
    val requiredRamMb: Int,
    val recommendedRamMb: Int,
    val sizeMb: Long,
    val downloadUrl: String? = null,
    val localPath: String?,
    val installState: ModelInstallState,
    val checksum: String?,
    val version: String,
    val supportsStructuredJson: Boolean,
    val supportsStreaming: Boolean,
    val supportsTextGeneration: Boolean,
    val isActive: Boolean = false,
    val installedAt: Long? = null
)

enum class ModelInstallState(val label: String) {
    NOT_INSTALLED("Not Installed"),
    DOWNLOADING("Downloading"),
    INSTALLING("Installing"),
    INSTALLED("Installed"),
    FAILED("Failed"),
    CORRUPTED("Corrupted")
}

enum class AiExecutionMode(val label: String, val description: String) {
    AUTO("Auto (Recommended)", "Automatically selects the best available AI engine."),
    SYSTEM_LOCAL("System AI", "Uses built-in on-device AI (e.g., Android AICore)."),
    CUSTOM_LOCAL("Local Model", "Runs a downloaded AI model directly on your device."),
    CLOUD("Cloud AI", "Sends summarized data to cloud AI for analysis.")
}

enum class RamTier(val minMb: Long) {
    LOW(0), MEDIUM(4096), HIGH(6144), VERY_HIGH(8192)
}

enum class DevicePerformanceClass {
    BASELINE, MODERATE, HIGH, FLAGSHIP
}

data class DeviceCapabilityResult(
    val androidVersion: Int,
    val sdkInt: Int,
    val aiCoreAvailable: Boolean,
    val aiCoreVersion: String?,
    val mlKitGenAiAvailable: Boolean,
    val ramTier: RamTier,
    val totalRamMb: Long,
    val availableStorageMb: Long,
    val performanceClass: DevicePerformanceClass,
    val supportedAbis: List<String>,
    val recommendedMode: AiExecutionMode
) {
    val supportsSystemAi get() = aiCoreAvailable || mlKitGenAiAvailable
    val supportsCustomLocalModel get() = ramTier >= RamTier.MEDIUM && availableStorageMb >= 500
}

data class CompatibilityReport(
    val isCompatible: Boolean,
    val issues: List<String>,
    val warnings: List<String>
)

data class BenchmarkResult(
    val modelId: String,
    val latencyMs: Long,
    val tokensPerSecond: Float,
    val memoryBeforeMb: Long,
    val memoryAfterMb: Long,
    val memoryDeltaMb: Long,
    val performanceRating: String
)

data class InstallProgress(
    val modelId: String,
    val state: ModelInstallState,
    val progressPercent: Int = 0,
    val errorMessage: String? = null
)
