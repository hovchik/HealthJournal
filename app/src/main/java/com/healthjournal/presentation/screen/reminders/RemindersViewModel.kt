package com.healthjournal.presentation.screen.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Reminder
import com.healthjournal.domain.model.ReminderFrequency
import com.healthjournal.domain.model.ReminderType
import com.healthjournal.util.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val reminderRepo = container.reminderRepository

    val reminders = reminderRepo.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(
        title: String,
        description: String = "",
        type: ReminderType = ReminderType.CUSTOM,
        frequency: ReminderFrequency = ReminderFrequency.DAILY,
        time: LocalTime = LocalTime.of(9, 0)
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                description = description,
                type = type,
                frequency = frequency,
                time = time
            )
            reminderRepo.insertReminder(reminder)
            ReminderWorker.schedule(getApplication())
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.updateReminder(reminder.copy(enabled = !reminder.enabled))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(reminder)
        }
    }
}
