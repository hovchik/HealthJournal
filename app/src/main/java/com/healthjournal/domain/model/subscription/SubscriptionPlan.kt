package com.healthjournal.domain.model.subscription

enum class SubscriptionPlan(
    val id: String,
    val monthlyPriceUsd: Double,
    val cloudAiReportsPerMonth: Int,
    val maxFamilyMembers: Int,
    val hasPdfExport: Boolean,
    val hasFullExportImport: Boolean,
    val hasCloudAi: Boolean,
    val hasUnlimitedCloudAi: Boolean
) {
    FREE(
        id = "free",
        monthlyPriceUsd = 0.0,
        cloudAiReportsPerMonth = 0,
        maxFamilyMembers = 1,
        hasPdfExport = false,
        hasFullExportImport = false,
        hasCloudAi = false,
        hasUnlimitedCloudAi = false
    ),
    BASIC(
        id = "basic_monthly",
        monthlyPriceUsd = 0.99,
        cloudAiReportsPerMonth = 0,
        maxFamilyMembers = Int.MAX_VALUE,
        hasPdfExport = true,
        hasFullExportImport = true,
        hasCloudAi = false,
        hasUnlimitedCloudAi = false
    ),
    PRO(
        id = "pro_monthly",
        monthlyPriceUsd = 3.99,
        cloudAiReportsPerMonth = 30,
        maxFamilyMembers = Int.MAX_VALUE,
        hasPdfExport = true,
        hasFullExportImport = true,
        hasCloudAi = true,
        hasUnlimitedCloudAi = false
    ),
    UNLIMITED(
        id = "unlimited_monthly",
        monthlyPriceUsd = 7.99,
        cloudAiReportsPerMonth = Int.MAX_VALUE,
        maxFamilyMembers = Int.MAX_VALUE,
        hasPdfExport = true,
        hasFullExportImport = true,
        hasCloudAi = true,
        hasUnlimitedCloudAi = true
    );

    companion object {
        fun fromId(id: String): SubscriptionPlan =
            entries.find { it.id == id } ?: FREE
    }
}
