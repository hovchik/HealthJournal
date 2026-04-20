package com.hovchik.healthjournal.domain.usecase

import com.hovchik.healthjournal.domain.model.subscription.SubscriptionPlan
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionStatus
import com.hovchik.healthjournal.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

class GetSubscriptionStatusUseCase(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<SubscriptionStatus> = repository.getSubscriptionStatus()
}

class SetSubscriptionPlanUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(plan: SubscriptionPlan) {
        repository.setSubscriptionPlan(plan)
    }
}

class IncrementCloudAiUsageUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke() {
        repository.resetMonthlyUsageIfNeeded()
        repository.incrementCloudAiUsage()
    }
}

class CheckCloudAiAccessUseCase(
    private val repository: SubscriptionRepository
) {
    suspend fun canUseCloudAi(): Boolean {
        repository.resetMonthlyUsageIfNeeded()
        var result = false
        repository.getSubscriptionStatus().collect { status ->
            result = status.canUseCloudAi
            return@collect
        }
        return result
    }
}
