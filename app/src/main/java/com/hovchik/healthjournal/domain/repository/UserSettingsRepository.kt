package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.UserSettings
import com.hovchik.healthjournal.domain.model.ai.AiSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings)
    suspend fun setAiConsent(consent: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setTermsAcceptedAt(timestamp: Long)
    suspend fun setLanguageMode(languageMode: String)
    suspend fun setThemeMode(themeMode: String)
    suspend fun setAiSettings(aiSettings: AiSettings)
    suspend fun setActiveProfileId(profileId: Long)
}
