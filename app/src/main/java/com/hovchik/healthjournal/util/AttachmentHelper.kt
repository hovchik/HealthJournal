package com.hovchik.healthjournal.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object AttachmentHelper {

    private fun attachmentsDir(context: Context): File {
        val dir = File(context.filesDir, "attachments")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun copyToInternal(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val ext = context.contentResolver.getType(uri)?.let { mime ->
                when {
                    mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
                    mime.contains("png") -> ".png"
                    mime.contains("pdf") -> ".pdf"
                    mime.contains("webp") -> ".webp"
                    else -> ""
                }
            } ?: ""
            val file = File(attachmentsDir(context), "${UUID.randomUUID()}$ext")
            file.outputStream().use { out -> inputStream.copyTo(out) }
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deleteAttachment(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }
}
