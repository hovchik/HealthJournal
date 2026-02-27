package com.healthjournal.domain.model.ai

import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import kotlinx.serialization.Serializable

enum class AiProviderId(val key: String) {
    CLAUDE("claude"),
    OPENAI_COMPATIBLE("openai"),
    GEMINI_NANO("gemini_nano"),
    LOCAL("local");

    companion object {
        fun fromKey(key: String): AiProviderId =
            entries.firstOrNull { it.key == key } ?: CLAUDE
    }
}

data class AiInput(
    val symptoms: List<Symptom>,
    val vitals: List<VitalSign>,
    val medications: List<Medication>,
    val periodDays: Int,
    val outputLanguage: String
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
    val model: String = "claude-sonnet-4-20250514",
    val baseUrl: String = "https://api.anthropic.com/",
    val timeoutSeconds: Int = 60,
    val streamingEnabled: Boolean = false
)

@Serializable
data class OpenAiConfig(
    val baseUrl: String = "https://api.openai.com/v1/",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
    val extraHeaders: Map<String, String> = emptyMap()
)

@Serializable
data class GeminiNanoConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 1024
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
    val selectedProviderId: String = AiProviderId.CLAUDE.key,
    val privacyRedactEnabled: Boolean = false,
    val claudeConfig: ClaudeConfig = ClaudeConfig(),
    val openAiConfig: OpenAiConfig = OpenAiConfig(),
    val geminiNanoConfig: GeminiNanoConfig = GeminiNanoConfig(),
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
