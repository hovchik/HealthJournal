package com.healthjournal.data.ai

import android.os.Build
import com.healthjournal.domain.model.ai.CompatibilityReport
import com.healthjournal.domain.model.ai.DeviceCapabilityResult
import com.healthjournal.domain.model.ai.LocalAiModel

class ModelCompatibilityValidator(
    private val detector: DeviceAiCapabilityDetector
) {
    fun validate(model: LocalAiModel, capability: DeviceCapabilityResult? = null): CompatibilityReport {
        val cap = capability ?: detector.detect()
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // RAM check
        if (cap.totalRamMb < model.requiredRamMb) {
            issues.add("Device has ${cap.totalRamMb} MB RAM, model requires ${model.requiredRamMb} MB")
        } else if (cap.totalRamMb < model.recommendedRamMb) {
            warnings.add("Device has ${cap.totalRamMb} MB RAM, model recommends ${model.recommendedRamMb} MB for best performance")
        }

        // Storage check
        val requiredStorage = model.sizeMb + 200
        if (cap.availableStorageMb < requiredStorage) {
            issues.add("Need ${requiredStorage} MB storage, only ${cap.availableStorageMb} MB available")
        }

        // ABI check
        if (model.runtimeType == "mediapipe_llm") {
            if ("arm64-v8a" !in cap.supportedAbis) {
                issues.add("MediaPipe LLM requires arm64-v8a architecture")
            }
        } else if (model.runtimeType == "litert") {
            if ("arm64-v8a" !in cap.supportedAbis && "armeabi-v7a" !in cap.supportedAbis) {
                issues.add("LiteRT requires arm64-v8a or armeabi-v7a architecture")
            }
        }

        // SDK check
        if (model.runtimeType == "mediapipe_llm" && Build.VERSION.SDK_INT < 24) {
            issues.add("MediaPipe LLM requires Android API 24+")
        }

        // Capability check
        if (!model.supportsTextGeneration) {
            issues.add("Model does not support text generation")
        }

        return CompatibilityReport(
            isCompatible = issues.isEmpty(),
            issues = issues,
            warnings = warnings
        )
    }
}
