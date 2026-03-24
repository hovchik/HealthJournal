package com.healthjournal.domain.model.ai

import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import kotlinx.serialization.Serializable

enum class AiProviderId(val key: String) {
    CLAUDE("claude"),
    OPENAI_COMPATIBLE("openai"),
    DEEPSEEK("deepseek"),
    GEMINI("gemini"),
    LOCAL("local");

    companion object {
        fun fromKey(key: String): AiProviderId =
            entries.firstOrNull { it.key == key } ?: LOCAL
    }
}

data class AiInput(
    val symptoms: List<Symptom>,
    val vitals: List<VitalSign>,
    val medications: List<Medication>,
    val periodDays: Int,
    val outputLanguage: String,
    val knownDiseases: List<String> = emptyList(),
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "",
    val diseaseName: String = ""
)

data class AiTextResult(
    val text: String,
    val providerId: AiProviderId,
    val modelUsed: String = ""
)

data class AiFlagsResult(
    val text: String,
    val providerId: AiProviderId,
    val modelUsed: String = ""
)

data class ValidationResult(
    val valid: Boolean,
    val errorMessage: String? = null
)

@Serializable
data class ClaudeConfig(
    val apiKey: String = "",
    val model: String = "claude-sonnet-4-6-20250627",
    val baseUrl: String = "https://api.anthropic.com/",
    val timeoutSeconds: Int = 60,
    val streamingEnabled: Boolean = false
)

@Serializable
data class OpenAiConfig(
    val baseUrl: String = "https://api.openai.com/v1/",
    val apiKey: String = "",
    val model: String = "gpt-4.1-mini",
    val extraHeaders: Map<String, String> = emptyMap()
)

@Serializable
data class DeepSeekConfig(
    val apiKey: String = "",
    val model: String = "deepseek-reasoner",
    val baseUrl: String = "https://api.deepseek.com/v1/",
    val timeoutSeconds: Int = 120
)

@Serializable
data class GeminiConfig(
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta/",
    val timeoutSeconds: Int = 90
)

@Serializable
data class LocalAiConfig(
    val modelPath: String = "",
    val contextSize: Int = 2048,
    val maxTokens: Int = 1024,
    val temperature: Float = 0.7f
)

@Serializable
data class AiSettings(
    val enabled: Boolean = true,
    val selectedProviderId: String = AiProviderId.LOCAL.key,
    val privacyRedactEnabled: Boolean = false,
    val claudeConfig: ClaudeConfig = ClaudeConfig(),
    val openAiConfig: OpenAiConfig = OpenAiConfig(),
    val deepSeekConfig: DeepSeekConfig = DeepSeekConfig(),
    val geminiConfig: GeminiConfig = GeminiConfig(),
    val localAiConfig: LocalAiConfig = LocalAiConfig()
)

data class LocalModelParams(
    val contextSize: Int = 2048,
    val maxTokens: Int = 1024,
    val temperature: Float = 0.7f
)

data class GenerationParams(
    val maxTokens: Int = 1024,
    val temperature: Float = 0.7f,
    val stopSequences: List<String> = emptyList()
)

/**
 * Represents a downloadable LLM model for on-device inference.
 */
@Serializable
data class DownloadableModel(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val url: String,
    val fileName: String
)

/**
 * State of an on-device model.
 */
sealed class ModelDownloadState {
    data object NotDownloaded : ModelDownloadState()
    data class Downloading(val progress: Float) : ModelDownloadState()
    data object Downloaded : ModelDownloadState()
    data class Error(val message: String) : ModelDownloadState()
}
