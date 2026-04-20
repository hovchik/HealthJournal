package com.hovchik.healthjournal.data.remote.api

import com.hovchik.healthjournal.data.remote.dto.ClaudeRequest
import com.hovchik.healthjournal.data.remote.dto.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApi {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): ClaudeResponse
}
