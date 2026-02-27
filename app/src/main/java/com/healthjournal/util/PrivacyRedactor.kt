package com.healthjournal.util

import com.healthjournal.domain.model.ai.AiInput

/**
 * Strips personally identifiable information from AI input:
 * phone numbers, emails, and common name patterns.
 */
class PrivacyRedactor {

    private val phonePattern = Regex("""(\+?\d[\d\s\-()]{6,}\d)""")
    private val emailPattern = Regex("""[\w.+-]+@[\w-]+\.[\w.]+""")

    fun redactInput(input: AiInput): AiInput = input.copy(
        symptoms = input.symptoms.map { s ->
            s.copy(
                notes = redactText(s.notes),
                triggers = s.triggers.map { redactText(it) }
            )
        },
        medications = input.medications.map { m ->
            m.copy(notes = redactText(m.notes))
        }
    )

    fun redactText(text: String): String {
        var result = text
        result = phonePattern.replace(result, "[PHONE]")
        result = emailPattern.replace(result, "[EMAIL]")
        return result
    }
}
