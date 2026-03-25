package com.healthjournal.presentation.screen.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.ai.AiProvider
import com.healthjournal.domain.model.ai.AiProviderId
import com.healthjournal.domain.model.ai.AiSettings
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
    private val aiProviderRegistry = container.aiProviderRegistry
    private val userSettingsRepository = container.userSettingsRepository

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedProviderId = MutableStateFlow<AiProviderId?>(null)
    val selectedProviderId = _selectedProviderId.asStateFlow()

    private val symptomRepo = container.symptomRepository
    private val vitalRepo = container.vitalSignRepository
    private val medicationRepo = container.medicationRepository

    val availableProviders: List<AiProvider> = aiProviderRegistry.getAll()

    init {
        viewModelScope.launch {
            val settings = userSettingsRepository.getUserSettings().first()
            val savedId = AiProviderId.fromKey(settings.aiSettings.selectedProviderId)
            _selectedProviderId.value = savedId
        }
    }

    fun selectProvider(providerId: AiProviderId) {
        _selectedProviderId.value = providerId
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val updatedMessages = _messages.value + ChatMessage(userMessage, isUser = true)
            _messages.value = updatedMessages
            _isLoading.value = true

            try {
                // Build health data context
                val symptoms = symptomRepo.getAllSymptoms().first().takeLast(10)
                val vitals = vitalRepo.getAllVitalSigns().first().takeLast(10)
                val meds = medicationRepo.getActiveMedications().first()

                val healthContext = buildString {
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

                // Build conversation history for context
                val conversationHistory = _messages.value.takeLast(10).joinToString("\n") { msg ->
                    if (msg.isUser) "User: ${msg.content}" else "Assistant: ${msg.content}"
                }

                val prompt = buildString {
                    if (healthContext.isNotBlank()) {
                        appendLine("[Health data: $healthContext]")
                    }
                    if (conversationHistory.isNotBlank()) {
                        appendLine(conversationHistory)
                    }
                    appendLine("User: $userMessage")
                }

                val settings = userSettingsRepository.getUserSettings().first()
                val response = aiService.generateResponse(
                    prompt = prompt,
                    settings = settings.aiSettings,
                    providerId = _selectedProviderId.value
                )
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
