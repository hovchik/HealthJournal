package com.hovchik.healthjournal.data.ai.runtime

interface LocalModelRuntime {
    val runtimeId: String
    val displayName: String
    fun isAvailable(): Boolean
    suspend fun runPrompt(prompt: String): String
    fun supportsStructuredJson(): Boolean
    fun supportsStreaming(): Boolean = false
    fun release()
}
