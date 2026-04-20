package com.hovchik.healthjournal.util

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
    val DISABLED_RELATIONS = stringSetPreferencesKey("disabled_relations")
    val DISABLED_GROUPS = stringSetPreferencesKey("disabled_groups")
    val CUSTOM_SYMPTOMS = stringSetPreferencesKey("custom_symptoms")
    val CUSTOM_MEDICATIONS = stringSetPreferencesKey("custom_medications")
    val CUSTOM_RELATIONS = stringSetPreferencesKey("custom_relations")
    val CUSTOM_GROUPS = stringSetPreferencesKey("custom_groups")
}
