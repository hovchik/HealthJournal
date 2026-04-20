package com.hovchik.healthjournal.data.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hovchik.healthjournal.domain.model.ai.AiExecutionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.aiPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_preferences")

class AiPreferences(private val context: Context) {

    private object Keys {
        val SELECTED_MODE = stringPreferencesKey("selected_ai_mode")
        val SETUP_COMPLETED = booleanPreferencesKey("local_ai_setup_completed")
        val ACTIVE_MODEL_ID = stringPreferencesKey("active_model_id")
    }

    fun observeSelectedMode(): Flow<AiExecutionMode> =
        context.aiPrefsDataStore.data.map { prefs ->
            prefs[Keys.SELECTED_MODE]?.let { key ->
                try { AiExecutionMode.valueOf(key) } catch (_: Exception) { AiExecutionMode.AUTO }
            } ?: AiExecutionMode.AUTO
        }

    suspend fun getSelectedMode(): AiExecutionMode =
        observeSelectedMode().first()

    suspend fun setSelectedMode(mode: AiExecutionMode) {
        context.aiPrefsDataStore.edit { prefs ->
            prefs[Keys.SELECTED_MODE] = mode.name
        }
    }

    fun observeSetupCompleted(): Flow<Boolean> =
        context.aiPrefsDataStore.data.map { prefs -> prefs[Keys.SETUP_COMPLETED] ?: false }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.aiPrefsDataStore.edit { prefs ->
            prefs[Keys.SETUP_COMPLETED] = completed
        }
    }

    fun observeActiveModelId(): Flow<String?> =
        context.aiPrefsDataStore.data.map { prefs -> prefs[Keys.ACTIVE_MODEL_ID] }

    suspend fun getActiveModelId(): String? =
        observeActiveModelId().first()

    suspend fun setActiveModelId(id: String?) {
        context.aiPrefsDataStore.edit { prefs ->
            if (id != null) prefs[Keys.ACTIVE_MODEL_ID] = id
            else prefs.remove(Keys.ACTIVE_MODEL_ID)
        }
    }
}
