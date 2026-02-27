package com.healthjournal.data.remote.openai

import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiApi {
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: OpenAiRequest): OpenAiResponse
}
