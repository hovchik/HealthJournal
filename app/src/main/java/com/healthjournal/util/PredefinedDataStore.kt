package com.healthjournal.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.predefinedDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "predefined_data_settings"
)

object PredefinedDataKeys {
    val DISABLED_SYMPTOMS = stringSetPreferencesKey("disabled_symptoms")
    val DISABLED_MEDICATIONS = stringSetPreferencesKey("disabled_medications")
    val CUSTOM_SYMPTOMS = stringSetPreferencesKey("custom_symptoms")
    val CUSTOM_MEDICATIONS = stringSetPreferencesKey("custom_medications")
}
