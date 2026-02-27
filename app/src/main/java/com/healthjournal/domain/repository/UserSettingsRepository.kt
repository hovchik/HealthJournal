package com.healthjournal.domain.repository

import com.healthjournal.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings)
    suspend fun setAiConsent(consent: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLanguageMode(languageMode: String)
}
