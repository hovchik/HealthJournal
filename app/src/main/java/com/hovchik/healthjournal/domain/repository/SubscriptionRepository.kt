package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.subscription.SubscriptionPlan
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscriptionStatus(): Flow<SubscriptionStatus>
    suspend fun setSubscriptionPlan(plan: SubscriptionPlan)
    suspend fun incrementCloudAiUsage()
    suspend fun resetMonthlyUsageIfNeeded()
}
