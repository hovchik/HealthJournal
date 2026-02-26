package com.healthjournal.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.healthjournal.domain.model.UserSettings
import com.healthjournal.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DOCTOR_NAME = stringPreferencesKey("doctor_name")
        val DOCTOR_PHONE = stringPreferencesKey("doctor_phone")
        val AI_CONSENT = booleanPreferencesKey("ai_consent")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override fun getUserSettings(): Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            userName = prefs[Keys.USER_NAME] ?: "",
            doctorName = prefs[Keys.DOCTOR_NAME] ?: "",
            doctorPhone = prefs[Keys.DOCTOR_PHONE] ?: "",
            aiConsentGiven = prefs[Keys.AI_CONSENT] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = settings.userName
            prefs[Keys.DOCTOR_NAME] = settings.doctorName
            prefs[Keys.DOCTOR_PHONE] = settings.doctorPhone
            prefs[Keys.AI_CONSENT] = settings.aiConsentGiven
            prefs[Keys.ONBOARDING_COMPLETED] = settings.onboardingCompleted
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
}
