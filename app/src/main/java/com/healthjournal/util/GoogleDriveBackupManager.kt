package com.healthjournal.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GoogleDriveBackupManager(private val context: Context) {

    companion object {
        private const val APP_FOLDER = "appDataFolder"
        private const val BACKUP_FILE_NAME = "health_journal_backup.json"
        private const val MIME_TYPE = "application/json"
    }

    private val signInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    }

    val signInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInIntent(): Intent = signInClient.signInIntent

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && account.grantedScopes.contains(Scope(DriveScopes.DRIVE_APPDATA))
    }

    fun getSignedInEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            signInClient.signOut()
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Health Journal")
            .build()
    }

    suspend fun uploadBackup(jsonData: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in"))

            val driveService = getDriveService(account)

            // Check if backup file already exists
            val existingFiles = driveService.files().list()
                .setSpaces(APP_FOLDER)
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, name)")
                .execute()

            val content = ByteArrayContent.fromString(MIME_TYPE, jsonData)

            if (existingFiles.files.isNotEmpty()) {
                // Update existing file
                val fileId = existingFiles.files[0].id
                driveService.files().update(fileId, null, content).execute()
                Result.success(fileId)
            } else {
                // Create new file
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf(APP_FOLDER)
                }
                val file = driveService.files().create(fileMetadata, content)
                    .setFields("id")
                    .execute()
                Result.success(file.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in"))

            val driveService = getDriveService(account)

            val files = driveService.files().list()
                .setSpaces(APP_FOLDER)
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            if (files.files.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("No backup found"))
            }

            val fileId = files.files[0].id
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val jsonData = outputStream.toString("UTF-8")
            Result.success(jsonData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLastBackupTime(): String? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null

            val driveService = getDriveService(account)

            val files = driveService.files().list()
                .setSpaces(APP_FOLDER)
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, modifiedTime)")
                .execute()

            if (files.files.isNullOrEmpty()) return@withContext null

            files.files[0].modifiedTime?.let {
                java.time.Instant.ofEpochMilli(it.value)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            }
        } catch (e: Exception) {
            null
        }
    }
}
