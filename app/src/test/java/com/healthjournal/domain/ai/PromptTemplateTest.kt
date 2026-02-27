package com.healthjournal.domain.ai

import com.healthjournal.domain.model.ai.AiInput
import org.junit.Assert.*
import org.junit.Test

class PromptTemplateTest {

    private val emptyInput = AiInput(
        symptoms = emptyList(),
        vitals = emptyList(),
        medications = emptyList(),
        periodDays = 7,
        outputLanguage = "en"
    )

    @Test
    fun `buildSummaryPrompt produces English prompt`() {
        val prompt = PromptTemplate.buildSummaryPrompt(emptyInput)
        assertTrue(prompt.system.contains("medical data assistant"))
        assertTrue(prompt.user.contains("SYMPTOMS"))
        assertTrue(prompt.user.contains("VITALS"))
        assertTrue(prompt.user.contains("MEDICATIONS"))
    }

    @Test
    fun `buildSummaryPrompt produces Russian prompt for unknown language`() {
        val input = emptyInput.copy(outputLanguage = "ru")
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        assertTrue(prompt.system.contains("медицинский ассистент"))
        assertTrue(prompt.user.contains("СИМПТОМЫ"))
    }

    @Test
    fun `buildSummaryPrompt produces Spanish prompt`() {
        val input = emptyInput.copy(outputLanguage = "es")
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        assertTrue(prompt.system.contains("asistente de datos medicos"))
        assertTrue(prompt.user.contains("SINTOMAS"))
    }

    @Test
    fun `buildSummaryPrompt produces Chinese prompt`() {
        val input = emptyInput.copy(outputLanguage = "zh-CN")
        val prompt = PromptTemplate.buildSummaryPrompt(input)
        assertTrue(prompt.system.contains("医疗数据助手"))
        assertTrue(prompt.user.contains("症状"))
    }

    @Test
    fun `buildPatternPrompt produces English prompt`() {
        val prompt = PromptTemplate.buildPatternPrompt(emptyInput)
        assertTrue(prompt.system.contains("medical data analyst"))
        assertTrue(prompt.user.contains("SYMPTOMS"))
        assertTrue(prompt.user.contains("VITALS"))
    }

    @Test
    fun `buildPatternPrompt produces Chinese prompt`() {
        val input = emptyInput.copy(outputLanguage = "zh-CN")
        val prompt = PromptTemplate.buildPatternPrompt(input)
        assertTrue(prompt.system.contains("医疗数据分析师"))
    }

    @Test
    fun `prompts include disclaimer`() {
        val prompt = PromptTemplate.buildSummaryPrompt(emptyInput)
        assertTrue(prompt.system.contains("NOT diagnose") || prompt.system.contains("IMPORTANT"))
    }
}
