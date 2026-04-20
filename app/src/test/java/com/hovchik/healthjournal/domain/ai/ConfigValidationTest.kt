package com.hovchik.healthjournal.domain.ai

import com.hovchik.healthjournal.data.ai.ClaudeAiProviderImpl
import com.hovchik.healthjournal.data.ai.OpenAiCompatibleProviderImpl
import com.hovchik.healthjournal.domain.model.ai.*
import org.junit.Assert.*
import org.junit.Test

class ConfigValidationTest {

    @Test
    fun `Claude config requires API key`() {
        val provider = ClaudeAiProviderImpl { throw IllegalStateException("should not be called") }
        val settings = AiSettings(claudeConfig = ClaudeConfig(apiKey = ""))
        val result = provider.validateConfig(settings)
        assertFalse(result.valid)
        assertTrue(result.errorMessage!!.contains("API key"))
    }

    @Test
    fun `Claude config valid with key`() {
        val provider = ClaudeAiProviderImpl { throw IllegalStateException("should not be called") }
        val settings = AiSettings(claudeConfig = ClaudeConfig(apiKey = "sk-test"))
        val result = provider.validateConfig(settings)
        assertTrue(result.valid)
    }

    @Test
    fun `OpenAI config requires API key`() {
        val provider = OpenAiCompatibleProviderImpl { throw IllegalStateException("should not be called") }
        val settings = AiSettings(openAiConfig = OpenAiConfig(apiKey = ""))
        val result = provider.validateConfig(settings)
        assertFalse(result.valid)
        assertTrue(result.errorMessage!!.contains("API key"))
    }

    @Test
    fun `OpenAI config valid with key`() {
        val provider = OpenAiCompatibleProviderImpl { throw IllegalStateException("should not be called") }
        val settings = AiSettings(openAiConfig = OpenAiConfig(apiKey = "sk-test"))
        val result = provider.validateConfig(settings)
        assertTrue(result.valid)
    }

    @Test
    fun `Claude provider requires online`() {
        val provider = ClaudeAiProviderImpl { throw IllegalStateException("should not be called") }
        assertTrue(provider.isOnlineRequired())
    }
}
