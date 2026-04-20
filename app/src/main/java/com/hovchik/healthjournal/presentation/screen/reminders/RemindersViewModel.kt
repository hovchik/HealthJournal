package com.hovchik.healthjournal.presentation.screen.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.domain.model.Reminder
import com.hovchik.healthjournal.domain.model.ReminderFrequency
import com.hovchik.healthjournal.domain.model.ReminderType
import com.hovchik.healthjournal.util.ReminderWorker
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
            val id = reminderRepo.insertReminder(reminder)

            val context = getApplication<Application>()
            // Ensure periodic worker is running
            ReminderWorker.schedule(context)

            // For ONCE reminders, also schedule a one-time work request for precision
            if (frequency == ReminderFrequency.ONCE) {
                ReminderWorker.scheduleOneTime(context, reminder.copy(id = id))
            }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(enabled = !reminder.enabled)
            reminderRepo.updateReminder(updated)

            val context = getApplication<Application>()
            if (!updated.enabled) {
                // Cancel one-time work if it was scheduled
                ReminderWorker.cancelReminder(context, reminder.id)
            } else if (updated.frequency == ReminderFrequency.ONCE && updated.lastTriggeredAt == null) {
                // Re-schedule one-time reminder if it hasn't fired yet
                ReminderWorker.scheduleOneTime(context, updated)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(reminder)
            // Cancel any scheduled work for this reminder
            val context = getApplication<Application>()
            ReminderWorker.cancelReminder(context, reminder.id)
        }
    }
}
