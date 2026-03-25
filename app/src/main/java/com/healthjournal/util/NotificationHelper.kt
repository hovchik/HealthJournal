package com.healthjournal.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.healthjournal.R
import com.healthjournal.domain.model.Reminder
import com.healthjournal.domain.model.ReminderType
import com.healthjournal.presentation.MainActivity

object NotificationHelper {

    private const val CHANNEL_MEDICATION = "medication_reminders"
    private const val CHANNEL_CHECK_IN = "check_in_reminders"
    private const val CHANNEL_APPOINTMENT = "appointment_reminders"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val medicationChannel = NotificationChannel(
            CHANNEL_MEDICATION,
            context.getString(R.string.channel_medication_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_medication_desc)
        }

        val checkInChannel = NotificationChannel(
            CHANNEL_CHECK_IN,
            context.getString(R.string.channel_checkin_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_checkin_desc)
        }

        val appointmentChannel = NotificationChannel(
            CHANNEL_APPOINTMENT,
            context.getString(R.string.channel_appointment_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_appointment_desc)
        }

        manager.createNotificationChannels(listOf(medicationChannel, checkInChannel, appointmentChannel))
    }

    fun showReminderNotification(context: Context, reminder: Reminder) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = when (reminder.type) {
            ReminderType.MEDICATION -> CHANNEL_MEDICATION
            ReminderType.APPOINTMENT -> CHANNEL_APPOINTMENT
            else -> CHANNEL_CHECK_IN
        }

        val icon = when (reminder.type) {
            ReminderType.MEDICATION -> R.drawable.ic_launcher_foreground
            ReminderType.SYMPTOM_CHECK -> R.drawable.ic_launcher_foreground
            ReminderType.VITAL_CHECK -> R.drawable.ic_launcher_foreground
            ReminderType.APPOINTMENT -> R.drawable.ic_launcher_foreground
            ReminderType.CUSTOM -> R.drawable.ic_launcher_foreground
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = reminder.description.ifBlank { reminder.title }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(reminder.title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(reminder.id.toInt(), notification)
    }

    fun showMissedDoseNotification(context: Context, medicationName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MEDICATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.missed_dose_title))
            .setContentText(context.getString(R.string.missed_dose_desc, medicationName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(medicationName.hashCode(), notification)
    }
}
