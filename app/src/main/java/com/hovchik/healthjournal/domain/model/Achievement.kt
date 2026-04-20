package com.hovchik.healthjournal.domain.model

import java.time.LocalDateTime

enum class AchievementType {
    STREAK_3_DAYS,
    STREAK_7_DAYS,
    STREAK_30_DAYS,
    STREAK_100_DAYS,
    FIRST_SYMPTOM,
    FIRST_VITAL,
    FIRST_MEDICATION,
    LOG_10_SYMPTOMS,
    LOG_50_SYMPTOMS,
    LOG_100_VITALS,
    FIRST_AI_REPORT,
    COMPLETE_PROFILE,
    ADD_FAMILY_MEMBER,
    FIRST_APPOINTMENT,
    EXPORT_DATA,
    SEVEN_DAY_VITALS
}

data class Achievement(
    val type: AchievementType,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: LocalDateTime? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
) {
    val isUnlocked: Boolean get() = unlockedAt != null
    val progressPercent: Float get() = if (maxProgress > 0) progress.toFloat() / maxProgress else 0f
}

data class GamificationStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = AchievementType.entries.size,
    val lastCheckInDate: LocalDateTime? = null
)
