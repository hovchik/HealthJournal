package com.healthjournal.data.remote

import com.healthjournal.data.remote.api.ClaudeApi
import com.healthjournal.data.remote.dto.ClaudeMessage
import com.healthjournal.data.remote.dto.ClaudeRequest
import com.healthjournal.domain.repository.AiProvider
import javax.inject.Inject

class ClaudeAiProvider @Inject constructor(
    private val api: ClaudeApi
) : AiProvider {

    override suspend fun generateSummary(prompt: String): Result<String> = runCatching {
        val request = ClaudeRequest(
            messages = listOf(
                ClaudeMessage(role = "user", content = prompt)
            )
        )
        val response = api.sendMessage(request)
        response.content.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from AI")
    }

    override suspend fun analyzePatterns(prompt: String): Result<String> = runCatching {
        val request = ClaudeRequest(
            messages = listOf(
                ClaudeMessage(role = "user", content = prompt)
            )
        )
        val response = api.sendMessage(request)
        response.content.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response from AI")
    }
}
