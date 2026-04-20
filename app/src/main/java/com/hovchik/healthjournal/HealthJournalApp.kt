package com.hovchik.healthjournal

import android.app.Application
import com.hovchik.healthjournal.di.AppContainer
import com.hovchik.healthjournal.util.LocaleManager
import com.hovchik.healthjournal.util.NotificationHelper
import com.hovchik.healthjournal.util.ReminderWorker
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

        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule reminder worker
        ReminderWorker.schedule(this)
    }
}
