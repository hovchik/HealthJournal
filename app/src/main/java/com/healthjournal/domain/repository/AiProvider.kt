package com.healthjournal.domain.repository

interface AiProvider {
    suspend fun generateSummary(prompt: String): Result<String>
    suspend fun analyzePatterns(prompt: String): Result<String>
}
