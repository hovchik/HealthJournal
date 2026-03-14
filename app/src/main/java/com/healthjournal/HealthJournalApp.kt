package com.healthjournal

import android.app.Application
import com.healthjournal.di.AppContainer
import com.healthjournal.util.LocaleManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HealthJournalApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Apply saved locale on app start
        val languageMode = runBlocking {
            container.userSettingsRepository.getUserSettings().first().languageMode
        }
        LocaleManager.applyLocale(languageMode)

        // Migrate GGUF models from wrong "mediapipe_llm" runtime to "llama_cpp"
        runBlocking { container.localModelManager.migrateGgufRuntimeType() }
    }
}
