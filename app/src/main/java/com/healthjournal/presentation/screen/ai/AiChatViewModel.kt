package com.healthjournal.presentation.screen.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiChatViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val aiService = container.aiService

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val symptomRepo = container.symptomRepository
    private val vitalRepo = container.vitalSignRepository
    private val medicationRepo = container.medicationRepository

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val updatedMessages = _messages.value + ChatMessage(userMessage, isUser = true)
            _messages.value = updatedMessages
            _isLoading.value = true

            try {
                // Build context from health data
                val symptoms = symptomRepo.getAllSymptoms().first().takeLast(10)
                val vitals = vitalRepo.getAllVitalSigns().first().takeLast(10)
                val meds = medicationRepo.getActiveMedications().first()

                val context = buildString {
                    appendLine("Recent health data context:")
                    if (symptoms.isNotEmpty()) {
                        appendLine("Recent symptoms: ${symptoms.joinToString { "${it.name} (${it.intensity}/10)" }}")
                    }
                    if (vitals.isNotEmpty()) {
                        appendLine("Recent vitals: ${vitals.joinToString { "${it.type.displayName}: ${it.value}" }}")
                    }
                    if (meds.isNotEmpty()) {
                        appendLine("Active medications: ${meds.joinToString { "${it.name} ${it.dosage}" }}")
                    }
                }

                val prompt = """You are a helpful health assistant for a health journal app.
You do NOT diagnose or prescribe. You help users understand their health data and suggest when to see a doctor.

$context

User question: $userMessage

Provide a helpful, concise response."""

                val response = aiService.generateResponse(prompt)
                _messages.value = _messages.value + ChatMessage(response, isUser = false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    "Error: ${e.message ?: "Failed to get response"}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}
