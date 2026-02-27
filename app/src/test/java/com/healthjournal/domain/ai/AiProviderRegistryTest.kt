package com.healthjournal.domain.ai

import com.healthjournal.domain.model.ai.*
import org.junit.Assert.*
import org.junit.Test

class AiProviderRegistryTest {

    private val fakeProviders = AiProviderId.entries.map { FakeAiProvider(it) }
    private val registry = AiProviderRegistry(fakeProviders)

    @Test
    fun `getAll returns all providers`() {
        assertEquals(4, registry.getAll().size)
    }

    @Test
    fun `getById returns correct provider`() {
        val provider = registry.getById(AiProviderId.OPENAI_COMPATIBLE)
        assertNotNull(provider)
        assertEquals(AiProviderId.OPENAI_COMPATIBLE, provider!!.id)
    }

    @Test
    fun `getById returns null for unregistered provider`() {
        val empty = AiProviderRegistry(emptyList())
        assertNull(empty.getById(AiProviderId.CLAUDE))
    }

    @Test
    fun `getByIdOrDefault returns first when not found`() {
        val partial = AiProviderRegistry(listOf(FakeAiProvider(AiProviderId.LOCAL)))
        val result = partial.getByIdOrDefault(AiProviderId.CLAUDE)
        assertEquals(AiProviderId.LOCAL, result.id)
    }

    @Test
    fun `AiProviderId fromKey parses correctly`() {
        assertEquals(AiProviderId.CLAUDE, AiProviderId.fromKey("claude"))
        assertEquals(AiProviderId.OPENAI_COMPATIBLE, AiProviderId.fromKey("openai"))
        assertEquals(AiProviderId.GEMINI_NANO, AiProviderId.fromKey("gemini_nano"))
        assertEquals(AiProviderId.LOCAL, AiProviderId.fromKey("local"))
        assertEquals(AiProviderId.CLAUDE, AiProviderId.fromKey("unknown"))
    }
}

private class FakeAiProvider(override val id: AiProviderId) : AiProvider {
    override val displayNameResId = 0

    override suspend fun generateDoctorSummary(input: AiInput, config: AiSettings) =
        AiTextResult("fake", id)

    override suspend fun analyzePatterns(input: AiInput, config: AiSettings) =
        AiFlagsResult("fake", id)

    override fun validateConfig(config: AiSettings) = ValidationResult(true)
    override fun isOnlineRequired() = false
}
