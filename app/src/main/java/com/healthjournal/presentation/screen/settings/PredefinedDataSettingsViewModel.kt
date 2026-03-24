package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.util.PredefinedDataKeys
import com.healthjournal.util.predefinedDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PredefinedDataSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.predefinedDataStore

    val disabledSymptoms: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val disabledMedications: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.DISABLED_MEDICATIONS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customSymptoms: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customMedications: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.CUSTOM_MEDICATIONS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val disabledRelations: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.DISABLED_RELATIONS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customRelations: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.CUSTOM_RELATIONS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val disabledGroups: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.DISABLED_GROUPS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val customGroups: StateFlow<Set<String>> = dataStore.data
        .map { it[PredefinedDataKeys.CUSTOM_GROUPS] ?: emptySet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleSymptom(key: String, enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.DISABLED_SYMPTOMS]?.toMutableSet() ?: mutableSetOf()
                if (enabled) current.remove(key) else current.add(key)
                prefs[PredefinedDataKeys.DISABLED_SYMPTOMS] = current
            }
        }
    }

    fun toggleMedication(key: String, enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.DISABLED_MEDICATIONS]?.toMutableSet() ?: mutableSetOf()
                if (enabled) current.remove(key) else current.add(key)
                prefs[PredefinedDataKeys.DISABLED_MEDICATIONS] = current
            }
        }
    }

    fun addCustomSymptom(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS]?.toMutableSet() ?: mutableSetOf()
                current.add(name)
                prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] = current
            }
        }
    }

    fun removeCustomSymptom(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS]?.toMutableSet() ?: mutableSetOf()
                current.remove(name)
                prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] = current
            }
        }
    }

    fun addCustomMedication(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS]?.toMutableSet() ?: mutableSetOf()
                current.add(name)
                prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] = current
            }
        }
    }

    fun removeCustomMedication(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS]?.toMutableSet() ?: mutableSetOf()
                current.remove(name)
                prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] = current
            }
        }
    }

    fun toggleRelation(key: String, enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.DISABLED_RELATIONS]?.toMutableSet() ?: mutableSetOf()
                if (enabled) current.remove(key) else current.add(key)
                prefs[PredefinedDataKeys.DISABLED_RELATIONS] = current
            }
        }
    }

    fun addCustomRelation(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_RELATIONS]?.toMutableSet() ?: mutableSetOf()
                current.add(name)
                prefs[PredefinedDataKeys.CUSTOM_RELATIONS] = current
            }
        }
    }

    fun removeCustomRelation(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_RELATIONS]?.toMutableSet() ?: mutableSetOf()
                current.remove(name)
                prefs[PredefinedDataKeys.CUSTOM_RELATIONS] = current
            }
        }
    }

    fun toggleGroup(key: String, enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.DISABLED_GROUPS]?.toMutableSet() ?: mutableSetOf()
                if (enabled) current.remove(key) else current.add(key)
                prefs[PredefinedDataKeys.DISABLED_GROUPS] = current
            }
        }
    }

    fun addCustomGroup(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_GROUPS]?.toMutableSet() ?: mutableSetOf()
                current.add(name)
                prefs[PredefinedDataKeys.CUSTOM_GROUPS] = current
            }
        }
    }

    fun removeCustomGroup(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PredefinedDataKeys.CUSTOM_GROUPS]?.toMutableSet() ?: mutableSetOf()
                current.remove(name)
                prefs[PredefinedDataKeys.CUSTOM_GROUPS] = current
            }
        }
    }
}
