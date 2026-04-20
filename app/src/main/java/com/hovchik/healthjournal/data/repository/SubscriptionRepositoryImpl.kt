package com.hovchik.healthjournal.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionPlan
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionStatus
import com.hovchik.healthjournal.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val Context.subscriptionDataStore by preferencesDataStore(name = "subscription_prefs")

class SubscriptionRepositoryImpl(
    private val context: Context
) : SubscriptionRepository {

    private object Keys {
        val PLAN_ID = stringPreferencesKey("plan_id")
        val CLOUD_AI_USAGE = intPreferencesKey("cloud_ai_usage")
        val MONTH_RESET_EPOCH = longPreferencesKey("month_reset_epoch")
        val EXPIRES_AT_EPOCH = longPreferencesKey("expires_at_epoch")
    }

    override fun getSubscriptionStatus(): Flow<SubscriptionStatus> =
        context.subscriptionDataStore.data.map { prefs ->
            val plan = SubscriptionPlan.fromId(prefs[Keys.PLAN_ID] ?: "free")
            val usage = prefs[Keys.CLOUD_AI_USAGE] ?: 0
            val resetEpoch = prefs[Keys.MONTH_RESET_EPOCH]
            val expiresEpoch = prefs[Keys.EXPIRES_AT_EPOCH]

            val resetDate = if (resetEpoch != null) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(resetEpoch), ZoneId.systemDefault())
            } else {
                LocalDateTime.now().withDayOfMonth(1)
            }

            val expiresAt = if (expiresEpoch != null) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(expiresEpoch), ZoneId.systemDefault())
            } else null

            val isActive = plan == SubscriptionPlan.FREE ||
                    (expiresAt != null && expiresAt.isAfter(LocalDateTime.now()))

            SubscriptionStatus(
                plan = plan,
                isActive = isActive,
                expiresAt = expiresAt,
                cloudAiReportsUsedThisMonth = usage,
                monthResetDate = resetDate
            )
        }

    override suspend fun setSubscriptionPlan(plan: SubscriptionPlan) {
        context.subscriptionDataStore.edit { prefs ->
            prefs[Keys.PLAN_ID] = plan.id
            if (plan != SubscriptionPlan.FREE) {
                val expiresAt = LocalDateTime.now().plusMonths(1)
                prefs[Keys.EXPIRES_AT_EPOCH] = expiresAt
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                prefs.remove(Keys.EXPIRES_AT_EPOCH)
            }
            prefs[Keys.CLOUD_AI_USAGE] = 0
            prefs[Keys.MONTH_RESET_EPOCH] = LocalDateTime.now().withDayOfMonth(1)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    override suspend fun incrementCloudAiUsage() {
        context.subscriptionDataStore.edit { prefs ->
            val current = prefs[Keys.CLOUD_AI_USAGE] ?: 0
            prefs[Keys.CLOUD_AI_USAGE] = current + 1
        }
    }

    override suspend fun resetMonthlyUsageIfNeeded() {
        context.subscriptionDataStore.edit { prefs ->
            val resetEpoch = prefs[Keys.MONTH_RESET_EPOCH]
            if (resetEpoch != null) {
                val resetDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(resetEpoch), ZoneId.systemDefault()
                )
                val now = LocalDateTime.now()
                if (now.month != resetDate.month || now.year != resetDate.year) {
                    prefs[Keys.CLOUD_AI_USAGE] = 0
                    prefs[Keys.MONTH_RESET_EPOCH] = now.withDayOfMonth(1)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }
    }
}
