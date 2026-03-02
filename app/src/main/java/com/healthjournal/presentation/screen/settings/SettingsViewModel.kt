package com.healthjournal.presentation.screen.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as HealthJournalApp).container
    private val exportImportManager = container.dataExportImportManager

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState = _uiState.asStateFlow()

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null)
            try {
                exportImportManager.exportData(uri)
                _uiState.value = _uiState.value.copy(isExporting = false, message = "export_success", isError = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, message = e.message, isError = true)
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null)
            try {
                exportImportManager.importData(uri)
                _uiState.value = _uiState.value.copy(isImporting = false, message = "import_success", isError = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isImporting = false, message = e.message, isError = true)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
