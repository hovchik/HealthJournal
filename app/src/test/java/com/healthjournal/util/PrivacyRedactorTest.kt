package com.healthjournal.util

import org.junit.Assert.*
import org.junit.Test

class PrivacyRedactorTest {

    private val redactor = PrivacyRedactor()

    @Test
    fun `redactText removes phone numbers`() {
        val input = "Call me at +1 555-123-4567 for details"
        val result = redactor.redactText(input)
        assertFalse(result.contains("555-123-4567"))
        assertTrue(result.contains("[PHONE]"))
    }

    @Test
    fun `redactText removes email addresses`() {
        val input = "Contact john.doe@example.com for info"
        val result = redactor.redactText(input)
        assertFalse(result.contains("john.doe@example.com"))
        assertTrue(result.contains("[EMAIL]"))
    }

    @Test
    fun `redactText preserves non-PII text`() {
        val input = "Headache started after lunch"
        val result = redactor.redactText(input)
        assertEquals(input, result)
    }

    @Test
    fun `redactText handles multiple patterns`() {
        val input = "Email: user@test.com, Phone: +7 999 123 4567"
        val result = redactor.redactText(input)
        assertTrue(result.contains("[EMAIL]"))
        assertTrue(result.contains("[PHONE]"))
        assertFalse(result.contains("user@test.com"))
        assertFalse(result.contains("999 123 4567"))
    }

    @Test
    fun `redactText handles empty string`() {
        assertEquals("", redactor.redactText(""))
    }
}
