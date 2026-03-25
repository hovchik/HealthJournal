package com.healthjournal.presentation.screen.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isSharing: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as HealthJournalApp).container
    private val exportImportManager = container.dataExportImportManager
    private val userSettingsRepository = container.userSettingsRepository

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState = _uiState.asStateFlow()

    val settings = userSettingsRepository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.healthjournal.domain.model.UserSettings())

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userSettingsRepository.setThemeMode(mode)
        }
    }

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

    fun shareData(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true, message = null)
            try {
                val jsonString = exportImportManager.exportToString()
                val file = withContext(Dispatchers.IO) {
                    val cacheDir = File(context.cacheDir, "shared")
                    cacheDir.mkdirs()
                    val f = File(cacheDir, "health_journal_backup.json")
                    f.writeText(jsonString, Charsets.UTF_8)
                    f
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, null))

                _uiState.value = _uiState.value.copy(isSharing = false, message = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSharing = false, message = e.message, isError = true)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null)
            try {
                exportImportManager.clearAllData()
                _uiState.value = _uiState.value.copy(isExporting = false, message = "clear_success", isError = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, message = e.message, isError = true)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
