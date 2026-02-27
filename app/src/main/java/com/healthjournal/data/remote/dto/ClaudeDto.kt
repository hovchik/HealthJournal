package com.healthjournal.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val system: String? = null,
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ClaudeContent>,
    val model: String,
    @SerialName("stop_reason") val stopReason: String? = null
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String = ""
)
