package com.healthjournal

import android.app.Application
import com.healthjournal.di.AppContainer

class HealthJournalApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
