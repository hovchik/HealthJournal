package com.hovchik.healthjournal.util

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Centralized locale management for the app.
 *
 * - Android 13+ (API 33): uses AppCompatDelegate.setApplicationLocales
 * - Pre-13: uses AppCompat locale support (best-effort via AppCompatDelegate)
 */
object LocaleManager {

    /** Supported language codes that map to resource qualifiers. */
    val supportedLanguages = listOf("ru", "en", "es", "zh-CN", "hy")

    /**
     * Applies the given language mode on app start.
     *
     * @param languageMode "SYSTEM" for system default, or a BCP-47 tag like "ru", "en", "es", "zh-CN"
     */
    fun applyLocale(languageMode: String) {
        if (languageMode == "SYSTEM") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val locale = parseLocale(languageMode)
            val localeList = LocaleListCompat.create(locale)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    /**
     * Returns the currently active locale for the app.
     */
    fun getCurrentLocale(context: Context): Locale {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (appLocales.isEmpty) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        } else {
            appLocales[0] ?: Locale.getDefault()
        }
    }

    /**
     * Returns the BCP-47 language tag for the current locale (e.g. "ru", "en", "es", "zh-CN").
     */
    fun getCurrentLanguageTag(context: Context): String {
        val locale = getCurrentLocale(context)
        return when {
            locale.language == "zh" -> "zh-CN"
            else -> locale.language
        }
    }

    /**
     * Returns the output language name used for AI prompts.
     * Maps language codes to full language names in that language.
     */
    fun getOutputLanguageName(languageTag: String): String = when (languageTag) {
        "ru" -> "русском"
        "en" -> "English"
        "es" -> "español"
        "zh-CN" -> "简体中文"
        "hy" -> "հայերեն"
        else -> "English"
    }

    private fun parseLocale(tag: String): Locale = when (tag) {
        "zh-CN" -> Locale.SIMPLIFIED_CHINESE
        else -> Locale.forLanguageTag(tag)
    }
}
