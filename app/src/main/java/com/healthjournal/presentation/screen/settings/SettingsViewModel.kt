package com.healthjournal.presentation.screen.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.healthjournal.HealthJournalApp
import com.healthjournal.util.BackupData
import com.healthjournal.util.GoogleDriveBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class ExportImportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isDriveUploading: Boolean = false,
    val isDriveDownloading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val googleSignedIn: Boolean = false,
    val googleEmail: String? = null,
    val lastDriveBackup: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as HealthJournalApp).container
    private val exportImportManager = container.dataExportImportManager
    val googleDriveManager = GoogleDriveBackupManager(application)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val _uiState = MutableStateFlow(ExportImportUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkGoogleSignIn()
    }

    private fun checkGoogleSignIn() {
        val signedIn = googleDriveManager.isSignedIn()
        _uiState.value = _uiState.value.copy(
            googleSignedIn = signedIn,
            googleEmail = if (signedIn) googleDriveManager.getSignedInEmail() else null
        )
        if (signedIn) {
            refreshLastBackupTime()
        }
    }

    private fun refreshLastBackupTime() {
        viewModelScope.launch {
            val time = googleDriveManager.getLastBackupTime()
            _uiState.value = _uiState.value.copy(lastDriveBackup = time)
        }
    }

    fun getGoogleSignInIntent(): Intent = googleDriveManager.getSignInIntent()

    fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            _uiState.value = _uiState.value.copy(
                googleSignedIn = true,
                googleEmail = account?.email,
                message = null
            )
            refreshLastBackupTime()
        } catch (e: ApiException) {
            _uiState.value = _uiState.value.copy(
                googleSignedIn = false,
                message = "Google Sign-In failed: ${e.statusCode}",
                isError = true
            )
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            googleDriveManager.signOut()
            _uiState.value = _uiState.value.copy(
                googleSignedIn = false,
                googleEmail = null,
                lastDriveBackup = null
            )
        }
    }

    fun uploadToDrive() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDriveUploading = true, message = null)
            try {
                val jsonData = exportImportManager.exportToString()
                val result = googleDriveManager.uploadBackup(jsonData)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isDriveUploading = false,
                            message = "export_success",
                            isError = false
                        )
                        refreshLastBackupTime()
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isDriveUploading = false,
                            message = e.message,
                            isError = true
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDriveUploading = false,
                    message = e.message,
                    isError = true
                )
            }
        }
    }

    fun downloadFromDrive() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDriveDownloading = true, message = null)
            try {
                val result = googleDriveManager.downloadBackup()
                result.fold(
                    onSuccess = { jsonData ->
                        exportImportManager.importFromString(jsonData)
                        _uiState.value = _uiState.value.copy(
                            isDriveDownloading = false,
                            message = "import_success",
                            isError = false
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isDriveDownloading = false,
                            message = e.message,
                            isError = true
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDriveDownloading = false,
                    message = e.message,
                    isError = true
                )
            }
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

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
