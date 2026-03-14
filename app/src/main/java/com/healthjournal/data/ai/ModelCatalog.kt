package com.healthjournal.data.ai

import com.healthjournal.domain.model.ai.LocalAiModel
import com.healthjournal.domain.model.ai.ModelInstallState

object ModelCatalog {

    /** All supported model file extensions for display in UI */
    val supportedFormats: List<String> = listOf(
        "GGUF", "GGML", "BIN"
    )

    val availableModels: List<LocalAiModel> = listOf(
        // ===== Small models (< 1 GB) =====
        LocalAiModel(
            modelId = "tinyllama-1.1b-q4",
            displayName = "TinyLlama 1.1B Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_0",
            requiredRamMb = 1500,
            recommendedRamMb = 2500,
            sizeMb = 600,
            downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_0.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "1.0",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),
        LocalAiModel(
            modelId = "stablelm-zephyr-1.6b-q4",
            displayName = "StableLM Zephyr 1.6B Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_0",
            requiredRamMb = 2000,
            recommendedRamMb = 3500,
            sizeMb = 950,
            downloadUrl = "https://huggingface.co/TheBloke/stablelm-zephyr-3b-GGUF/resolve/main/stablelm-zephyr-3b.Q4_0.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "1.0",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),

        // ===== Medium models (1–3 GB) — recommended tier =====
        LocalAiModel(
            modelId = "qwen2.5-3b-instruct-q4",
            displayName = "Qwen 2.5 3B Instruct Q4 (Recommended)",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 3500,
            recommendedRamMb = 5000,
            sizeMb = 2100,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "2.5",
            supportsStructuredJson = true,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),
        LocalAiModel(
            modelId = "phi-2-2.7b-q4",
            displayName = "Phi-2 2.7B Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 3000,
            recommendedRamMb = 4500,
            sizeMb = 1700,
            downloadUrl = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q4_K_M.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "2.0",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),
        LocalAiModel(
            modelId = "rocket-3b-q4",
            displayName = "Rocket 3B Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 3200,
            recommendedRamMb = 4800,
            sizeMb = 1900,
            downloadUrl = "https://huggingface.co/TheBloke/rocket-3B-GGUF/resolve/main/rocket-3b.Q4_K_M.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "1.0",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),

        // ===== Large models (3+ GB) =====
        LocalAiModel(
            modelId = "mistral-7b-instruct-q4",
            displayName = "Mistral 7B Instruct Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 6000,
            recommendedRamMb = 8000,
            sizeMb = 4100,
            downloadUrl = "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q4_K_M.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "0.2",
            supportsStructuredJson = true,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),
        LocalAiModel(
            modelId = "zephyr-7b-beta-q4",
            displayName = "Zephyr 7B Beta Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 6000,
            recommendedRamMb = 8000,
            sizeMb = 4100,
            downloadUrl = "https://huggingface.co/TheBloke/zephyr-7B-beta-GGUF/resolve/main/zephyr-7b-beta.Q4_K_M.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "1.0",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        ),
        LocalAiModel(
            modelId = "openchat-3.5-7b-q4",
            displayName = "OpenChat 3.5 7B Q4",
            runtimeType = "llama_cpp",
            fileFormat = "gguf",
            quantization = "Q4_K_M",
            requiredRamMb = 6000,
            recommendedRamMb = 8000,
            sizeMb = 4100,
            downloadUrl = "https://huggingface.co/TheBloke/openchat-3.5-0106-GGUF/resolve/main/openchat-3.5-0106.Q4_K_M.gguf",
            localPath = null,
            installState = ModelInstallState.NOT_INSTALLED,
            checksum = null,
            version = "3.5",
            supportsStructuredJson = false,
            supportsStreaming = true,
            supportsTextGeneration = true
        )
    )

    fun getById(modelId: String): LocalAiModel? =
        availableModels.firstOrNull { it.modelId == modelId }
}
