package com.healthjournal.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Reminder
import com.healthjournal.domain.model.ReminderFrequency
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as HealthJournalApp
            val reminderRepo = app.container.reminderRepository

            val reminders = reminderRepo.getEnabledReminders().first()
            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val currentDayOfWeek = now.dayOfWeek.value
            val currentDayOfMonth = now.dayOfMonth

            for (reminder in reminders) {
                if (!shouldFireNow(reminder, currentTime, currentDayOfWeek, currentDayOfMonth, now)) continue

                // Prevent duplicate notifications within the same period
                val lastTriggered = reminder.lastTriggeredAt
                if (lastTriggered != null) {
                    val minutesSinceLastTrigger = Duration.between(lastTriggered, now).toMinutes()
                    if (minutesSinceLastTrigger < 10) continue
                }

                NotificationHelper.showReminderNotification(applicationContext, reminder)

                // Update lastTriggeredAt
                val updatedReminder = reminder.copy(lastTriggeredAt = now)

                // For ONCE frequency, disable after firing
                if (reminder.frequency == ReminderFrequency.ONCE) {
                    reminderRepo.updateReminder(updatedReminder.copy(enabled = false))
                } else {
                    reminderRepo.updateReminder(updatedReminder)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Reminder worker failed", e)
            Result.retry()
        }
    }

    private fun shouldFireNow(
        reminder: Reminder,
        currentTime: LocalTime,
        currentDayOfWeek: Int,
        currentDayOfMonth: Int,
        now: LocalDateTime
    ): Boolean {
        // Check time window (within 7 minutes to account for 15-min worker interval)
        val timeDiff = Duration.between(reminder.time, currentTime).abs()
        if (timeDiff.toMinutes() > 7) return false

        return when (reminder.frequency) {
            ReminderFrequency.ONCE -> {
                // ONCE: fire if not yet triggered
                reminder.lastTriggeredAt == null
            }
            ReminderFrequency.DAILY -> {
                // DAILY: fire if today is in daysOfWeek
                currentDayOfWeek in reminder.daysOfWeek
            }
            ReminderFrequency.WEEKLY -> {
                // WEEKLY: fire only on specified days of week
                currentDayOfWeek in reminder.daysOfWeek
            }
            ReminderFrequency.MONTHLY -> {
                // MONTHLY: fire on the same day of month as creation date
                val createdDayOfMonth = reminder.createdAt.dayOfMonth
                currentDayOfMonth == createdDayOfMonth
            }
        }
    }

    companion object {
        private const val TAG = "ReminderWorker"
        private const val WORK_NAME = "reminder_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun scheduleOneTime(context: Context, reminder: Reminder) {
            val now = LocalDateTime.now()
            val targetTime = LocalDateTime.of(now.toLocalDate(), reminder.time)
            val delay = if (targetTime.isAfter(now)) {
                Duration.between(now, targetTime).toMillis()
            } else {
                // If time already passed today, schedule for tomorrow
                Duration.between(now, targetTime.plusDays(1)).toMillis()
            }

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("reminder_${reminder.id}")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancelReminder(context: Context, reminderId: Long) {
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$reminderId")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
