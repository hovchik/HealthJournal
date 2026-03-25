package com.healthjournal.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.healthjournal.HealthJournalApp
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class HealthConnectSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HealthJournalApp ?: return Result.failure()
        val container = app.container
        val healthConnectManager = container.healthConnectManager

        if (!healthConnectManager.isAvailable) {
            Log.w(TAG, "Health Connect not available, skipping sync")
            return Result.success()
        }

        return try {
            val hasPermissions = healthConnectManager.hasAllPermissions()
            if (!hasPermissions) {
                Log.w(TAG, "Health Connect permissions not granted, skipping sync")
                return Result.success()
            }

            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            val vitals = healthConnectManager.importAllRecent(profileId)

            for (vital in vitals) {
                container.addVitalSign(vital)
            }

            Log.i(TAG, "Health Connect sync completed: ${vitals.size} records imported")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Health Connect sync failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HealthConnectSync"
        private const val WORK_NAME = "health_connect_sync"

        fun schedule(context: Context, intervalHours: Long = 6) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<HealthConnectSyncWorker>(
                intervalHours, TimeUnit.HOURS
            ).setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )

            Log.i(TAG, "Health Connect sync scheduled every $intervalHours hours")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.i(TAG, "Health Connect sync cancelled")
        }

        fun syncNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<HealthConnectSyncWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun isEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("health_connect_sync", Context.MODE_PRIVATE)
            return prefs.getBoolean("auto_sync_enabled", false)
        }

        fun setEnabled(context: Context, enabled: Boolean, intervalHours: Long = 6) {
            val prefs = context.getSharedPreferences("health_connect_sync", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("auto_sync_enabled", enabled).apply()
            if (enabled) {
                schedule(context, intervalHours)
            } else {
                cancel(context)
            }
        }

        fun getSyncInterval(context: Context): Long {
            val prefs = context.getSharedPreferences("health_connect_sync", Context.MODE_PRIVATE)
            return prefs.getLong("sync_interval_hours", 6)
        }

        fun setSyncInterval(context: Context, intervalHours: Long) {
            val prefs = context.getSharedPreferences("health_connect_sync", Context.MODE_PRIVATE)
            prefs.edit().putLong("sync_interval_hours", intervalHours).apply()
            if (isEnabled(context)) {
                schedule(context, intervalHours)
            }
        }
    }
}
