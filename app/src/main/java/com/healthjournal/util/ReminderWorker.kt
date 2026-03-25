package com.healthjournal.util

import android.content.Context
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
        val app = applicationContext as HealthJournalApp
        val reminderRepo = app.container.reminderRepository

        val reminders = reminderRepo.getEnabledReminders().first()
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()

        for (reminder in reminders) {
            val dayOfWeek = now.dayOfWeek.value
            if (dayOfWeek !in reminder.daysOfWeek) continue

            val timeDiff = Duration.between(reminder.time, currentTime).abs()
            if (timeDiff.toMinutes() <= 5) {
                NotificationHelper.showReminderNotification(applicationContext, reminder)
            }
        }

        return Result.success()
    }

    companion object {
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
                Duration.between(now, targetTime.plusDays(1)).toMillis()
            }

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("reminder_${reminder.id}")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
