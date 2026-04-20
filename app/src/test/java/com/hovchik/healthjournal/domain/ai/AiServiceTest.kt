package com.hovchik.healthjournal.domain.ai

import com.hovchik.healthjournal.domain.model.ai.*
import com.hovchik.healthjournal.util.PrivacyRedactor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AiServiceTest {

    private val fakeProvider = object : AiProvider {
        override val id = AiProviderId.CLAUDE
        override val displayNameResId = 0
        var lastInput: AiInput? = null

        override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings): AiTextResult {
            lastInput = input
            return AiTextResult("summary", id, "test-model")
        }

        override suspend fun analyzePatterns(input: AiInput, config: AiSettings): AiFlagsResult {
            lastInput = input
            return AiFlagsResult("patterns", id, "test-model")
        }

        override fun validateConfig(config: AiSettings) = ValidationResult(true)
        override fun isOnlineRequired() = true
    }

    private val registry = AiProviderRegistry(listOf(fakeProvider))
    private val redactor = PrivacyRedactor()
    private val service = AiService(registry, redactor)

    private val testInput = AiInput(
        symptoms = emptyList(),
        vitals = emptyList(),
        medications = emptyList(),
        periodDays = 7,
        outputLanguage = "en"
    )

    @Test
    fun `getActiveProvider returns selected provider`() {
        val settings = AiSettings(selectedProviderId = "claude")
        val provider = service.getActiveProvider(settings)
        assertEquals(AiProviderId.CLAUDE, provider.id)
    }

    @Test
    fun `validateActiveProvider returns valid result`() {
        val settings = AiSettings(selectedProviderId = "claude")
        val result = service.validateActiveProvider(settings)
        assertTrue(result.valid)
    }

    @Test
    fun `generateDoctorSummary delegates to provider`() = runBlocking {
        val settings = AiSettings(selectedProviderId = "claude")
        val result = service.generateDoctorSummary(testInput, settings)
        assertEquals("summary", result.text)
        assertEquals(AiProviderId.CLAUDE, result.providerId)
    }

    @Test
    fun `analyzePatterns delegates to provider`() = runBlocking {
        val settings = AiSettings(selectedProviderId = "claude")
        val result = service.analyzePatterns(testInput, settings)
        assertEquals("patterns", result.text)
    }

    @Test
    fun `getAllProviders returns all registered providers`() {
        assertEquals(1, service.getAllProviders().size)
    }
}
