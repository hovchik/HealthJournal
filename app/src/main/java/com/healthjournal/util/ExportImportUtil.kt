package com.healthjournal.util

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class ExportMeta(
    val appVersion: String = "1.0",
    val exportTimestamp: Long = System.currentTimeMillis(),
    val locale: String = Locale.getDefault().toLanguageTag(),
    val uiLanguage: String = "SYSTEM"
)

object ExportImportUtil {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Builds the meta.json content for export.
     * Includes locale and uiLanguage fields for language-agnostic export.
     *
     * Note: On import, do NOT change the user's UI language automatically;
     * keep the user's current preference.
     */
    fun buildExportMeta(context: Context, languageMode: String): String {
        val meta = ExportMeta(
            locale = Locale.getDefault().toLanguageTag(),
            uiLanguage = languageMode
        )
        return json.encodeToString(ExportMeta.serializer(), meta)
    }

    /**
     * Parses the meta.json content from an import.
     * Returns null if parsing fails.
     */
    fun parseImportMeta(metaJson: String): ExportMeta? = try {
        json.decodeFromString(ExportMeta.serializer(), metaJson)
    } catch (_: Exception) {
        null
    }
}
