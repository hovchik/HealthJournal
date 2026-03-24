package com.healthjournal.domain.model.subscription

import java.time.LocalDateTime

data class SubscriptionStatus(
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val isActive: Boolean = true,
    val expiresAt: LocalDateTime? = null,
    val cloudAiReportsUsedThisMonth: Int = 0,
    val monthResetDate: LocalDateTime = LocalDateTime.now().withDayOfMonth(1)
) {
    val canUseCloudAi: Boolean
        get() = plan.hasCloudAi && isActive &&
                (plan.hasUnlimitedCloudAi || cloudAiReportsUsedThisMonth < plan.cloudAiReportsPerMonth)

    val cloudAiReportsRemaining: Int
        get() = if (plan.hasUnlimitedCloudAi) Int.MAX_VALUE
        else (plan.cloudAiReportsPerMonth - cloudAiReportsUsedThisMonth).coerceAtLeast(0)

    val canAddFamilyMember: Boolean
        get() = plan.maxFamilyMembers == Int.MAX_VALUE

    val canExportPdf: Boolean
        get() = plan.hasPdfExport && isActive

    val canFullExportImport: Boolean
        get() = plan.hasFullExportImport && isActive
}
