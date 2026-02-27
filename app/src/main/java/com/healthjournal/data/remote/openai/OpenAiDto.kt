package com.healthjournal.data.remote.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val temperature: Double = 0.7
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiResponse(
    val id: String = "",
    val model: String = "",
    val choices: List<OpenAiChoice> = emptyList()
)

@Serializable
data class OpenAiChoice(
    val index: Int = 0,
    val message: OpenAiMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)
