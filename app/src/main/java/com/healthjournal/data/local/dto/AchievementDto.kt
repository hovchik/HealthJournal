package com.healthjournal.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class AchievementDto(
    val type: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0
)

@Serializable
data class GamificationStatsDto(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val lastCheckInDate: Long? = null,
    val achievements: List<AchievementDto> = emptyList()
)
