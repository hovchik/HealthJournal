package com.healthjournal.domain.ai

import com.healthjournal.data.ai.ClaudeAiProviderImpl
import com.healthjournal.data.ai.LocalAiProviderImpl
import com.healthjournal.data.ai.OpenAiCompatibleProviderImpl
import com.healthjournal.data.ai.StubLocalInferenceEngine
import com.healthjournal.domain.model.ai.*
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
    fun `Local config requires model path`() {
        val provider = LocalAiProviderImpl(StubLocalInferenceEngine())
        val settings = AiSettings(localAiConfig = LocalAiConfig(modelPath = ""))
        val result = provider.validateConfig(settings)
        assertFalse(result.valid)
        assertTrue(result.errorMessage!!.contains("Model path"))
    }

    @Test
    fun `Local config valid with path`() {
        val provider = LocalAiProviderImpl(StubLocalInferenceEngine())
        val settings = AiSettings(localAiConfig = LocalAiConfig(modelPath = "/data/model.gguf"))
        val result = provider.validateConfig(settings)
        assertTrue(result.valid)
    }

    @Test
    fun `Claude provider requires online`() {
        val provider = ClaudeAiProviderImpl { throw IllegalStateException("should not be called") }
        assertTrue(provider.isOnlineRequired())
    }

    @Test
    fun `Local provider does not require online`() {
        val provider = LocalAiProviderImpl(StubLocalInferenceEngine())
        assertFalse(provider.isOnlineRequired())
    }
}
