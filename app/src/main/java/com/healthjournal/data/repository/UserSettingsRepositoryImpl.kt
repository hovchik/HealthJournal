package com.healthjournal.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.healthjournal.domain.model.UserSettings
import com.healthjournal.domain.model.ai.AiSettings
import com.healthjournal.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepositoryImpl constructor(
    private val context: Context
) : UserSettingsRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DOCTOR_NAME = stringPreferencesKey("doctor_name")
        val DOCTOR_PHONE = stringPreferencesKey("doctor_phone")
        val WEIGHT = stringPreferencesKey("weight")
        val HEIGHT = stringPreferencesKey("height")
        val KNOWN_DISEASES = stringPreferencesKey("known_diseases")
        val AI_CONSENT = booleanPreferencesKey("ai_consent")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AI_SETTINGS = stringPreferencesKey("ai_settings")
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
    }

    override fun getUserSettings(): Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val aiSettingsJson = prefs[Keys.AI_SETTINGS]
        val aiSettings = if (aiSettingsJson != null) {
            try { json.decodeFromString<AiSettings>(aiSettingsJson) } catch (_: Exception) { AiSettings() }
        } else AiSettings()

        val knownDiseasesJson = prefs[Keys.KNOWN_DISEASES] ?: "[]"
        val knownDiseases = try {
            json.decodeFromString(ListSerializer(String.serializer()), knownDiseasesJson)
        } catch (_: Exception) { emptyList() }

        UserSettings(
            userName = prefs[Keys.USER_NAME] ?: "",
            doctorName = prefs[Keys.DOCTOR_NAME] ?: "",
            doctorPhone = prefs[Keys.DOCTOR_PHONE] ?: "",
            weight = prefs[Keys.WEIGHT] ?: "",
            height = prefs[Keys.HEIGHT] ?: "",
            knownDiseases = knownDiseases,
            aiConsentGiven = prefs[Keys.AI_CONSENT] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            languageMode = prefs[Keys.LANGUAGE_MODE] ?: "SYSTEM",
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            aiSettings = aiSettings,
            activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: 0
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = settings.userName
            prefs[Keys.DOCTOR_NAME] = settings.doctorName
            prefs[Keys.DOCTOR_PHONE] = settings.doctorPhone
            prefs[Keys.WEIGHT] = settings.weight
            prefs[Keys.HEIGHT] = settings.height
            prefs[Keys.KNOWN_DISEASES] = json.encodeToString(
                ListSerializer(String.serializer()), settings.knownDiseases
            )
            prefs[Keys.AI_CONSENT] = settings.aiConsentGiven
            prefs[Keys.ONBOARDING_COMPLETED] = settings.onboardingCompleted
            prefs[Keys.LANGUAGE_MODE] = settings.languageMode
            prefs[Keys.THEME_MODE] = settings.themeMode
            prefs[Keys.AI_SETTINGS] = json.encodeToString(AiSettings.serializer(), settings.aiSettings)
        }
    }

    override suspend fun setAiConsent(consent: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_CONSENT] = consent
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setLanguageMode(languageMode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_MODE] = languageMode
        }
    }

    override suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode
        }
    }

    override suspend fun setAiSettings(aiSettings: AiSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_SETTINGS] = json.encodeToString(AiSettings.serializer(), aiSettings)
        }
    }

    override suspend fun setActiveProfileId(profileId: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_PROFILE_ID] = profileId
        }
    }
}
