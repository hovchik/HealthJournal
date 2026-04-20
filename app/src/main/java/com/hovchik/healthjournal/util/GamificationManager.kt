package com.hovchik.healthjournal.util

import android.content.Context
import com.hovchik.healthjournal.data.local.dto.AchievementDto
import com.hovchik.healthjournal.data.local.dto.GamificationStatsDto
import com.hovchik.healthjournal.domain.model.Achievement
import com.hovchik.healthjournal.domain.model.AchievementType
import com.hovchik.healthjournal.domain.model.GamificationStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class GamificationManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("gamification", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _stats = MutableStateFlow(loadStats())
    val stats: Flow<GamificationStats> = _stats.asStateFlow()

    private fun loadStats(): GamificationStats {
        val raw = prefs.getString("stats", null) ?: return GamificationStats()
        return try {
            val dto = json.decodeFromString(GamificationStatsDto.serializer(), raw)
            GamificationStats(
                currentStreak = dto.currentStreak,
                longestStreak = dto.longestStreak,
                totalCheckIns = dto.totalCheckIns,
                achievementsUnlocked = dto.achievements.count { it.unlockedAt != null },
                lastCheckInDate = dto.lastCheckInDate?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                }
            )
        } catch (e: Exception) {
            GamificationStats()
        }
    }

    private fun loadAchievements(): List<AchievementDto> {
        val raw = prefs.getString("stats", null) ?: return emptyList()
        return try {
            json.decodeFromString(GamificationStatsDto.serializer(), raw).achievements
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun recordCheckIn() {
        val current = _stats.value
        val today = LocalDate.now()
        val lastDate = current.lastCheckInDate?.toLocalDate()

        val newStreak = when {
            lastDate == null -> 1
            lastDate == today -> current.currentStreak
            lastDate == today.minusDays(1) -> current.currentStreak + 1
            else -> 1
        }

        val newStats = current.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(current.longestStreak, newStreak),
            totalCheckIns = if (lastDate != today) current.totalCheckIns + 1 else current.totalCheckIns,
            lastCheckInDate = LocalDateTime.now()
        )

        saveStats(newStats)
        _stats.value = newStats

        checkStreakAchievements(newStreak)
    }

    private fun checkStreakAchievements(streak: Int) {
        if (streak >= 3) unlockAchievement(AchievementType.STREAK_3_DAYS)
        if (streak >= 7) unlockAchievement(AchievementType.STREAK_7_DAYS)
        if (streak >= 30) unlockAchievement(AchievementType.STREAK_30_DAYS)
        if (streak >= 100) unlockAchievement(AchievementType.STREAK_100_DAYS)
    }

    fun checkDataAchievements(symptomCount: Int, vitalCount: Int, medicationCount: Int, reportCount: Int) {
        if (symptomCount >= 1) unlockAchievement(AchievementType.FIRST_SYMPTOM)
        if (vitalCount >= 1) unlockAchievement(AchievementType.FIRST_VITAL)
        if (medicationCount >= 1) unlockAchievement(AchievementType.FIRST_MEDICATION)
        if (symptomCount >= 10) unlockAchievement(AchievementType.LOG_10_SYMPTOMS)
        if (symptomCount >= 50) unlockAchievement(AchievementType.LOG_50_SYMPTOMS)
        if (vitalCount >= 100) unlockAchievement(AchievementType.LOG_100_VITALS)
        if (reportCount >= 1) unlockAchievement(AchievementType.FIRST_AI_REPORT)
    }

    fun unlockAchievement(type: AchievementType) {
        val achievements = loadAchievements().toMutableList()
        val existing = achievements.find { it.type == type.name }
        if (existing?.unlockedAt != null) return

        val now = Instant.now().toEpochMilli()
        if (existing != null) {
            achievements.remove(existing)
            achievements.add(existing.copy(unlockedAt = now))
        } else {
            achievements.add(AchievementDto(type = type.name, unlockedAt = now, progress = 1))
        }

        val current = _stats.value
        val newStats = current.copy(achievementsUnlocked = achievements.count { it.unlockedAt != null })
        _stats.value = newStats

        val dto = GamificationStatsDto(
            currentStreak = newStats.currentStreak,
            longestStreak = newStats.longestStreak,
            totalCheckIns = newStats.totalCheckIns,
            lastCheckInDate = newStats.lastCheckInDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            achievements = achievements
        )
        prefs.edit().putString("stats", json.encodeToString(GamificationStatsDto.serializer(), dto)).apply()
    }

    fun getAllAchievements(): List<Achievement> {
        val saved = loadAchievements()
        return AchievementType.entries.map { type ->
            val dto = saved.find { it.type == type.name }
            Achievement(
                type = type,
                title = type.toTitle(),
                description = type.toDescription(),
                icon = type.toIcon(),
                unlockedAt = dto?.unlockedAt?.let {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                },
                progress = dto?.progress ?: 0,
                maxProgress = type.toMaxProgress()
            )
        }
    }

    private fun saveStats(stats: GamificationStats) {
        val achievements = loadAchievements()
        val dto = GamificationStatsDto(
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak,
            totalCheckIns = stats.totalCheckIns,
            lastCheckInDate = stats.lastCheckInDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            achievements = achievements
        )
        prefs.edit().putString("stats", json.encodeToString(GamificationStatsDto.serializer(), dto)).apply()
    }
}

private fun AchievementType.toTitle(): String = when (this) {
    AchievementType.STREAK_3_DAYS -> "3-Day Streak"
    AchievementType.STREAK_7_DAYS -> "Week Warrior"
    AchievementType.STREAK_30_DAYS -> "Monthly Master"
    AchievementType.STREAK_100_DAYS -> "Century Champion"
    AchievementType.FIRST_SYMPTOM -> "First Log"
    AchievementType.FIRST_VITAL -> "Vital Start"
    AchievementType.FIRST_MEDICATION -> "Medicine Tracker"
    AchievementType.LOG_10_SYMPTOMS -> "Symptom Spotter"
    AchievementType.LOG_50_SYMPTOMS -> "Health Detective"
    AchievementType.LOG_100_VITALS -> "Vital Expert"
    AchievementType.FIRST_AI_REPORT -> "AI Explorer"
    AchievementType.COMPLETE_PROFILE -> "Profile Pro"
    AchievementType.ADD_FAMILY_MEMBER -> "Family Care"
    AchievementType.FIRST_APPOINTMENT -> "Doctor Visit"
    AchievementType.EXPORT_DATA -> "Data Guardian"
    AchievementType.SEVEN_DAY_VITALS -> "Week of Vitals"
}

private fun AchievementType.toDescription(): String = when (this) {
    AchievementType.STREAK_3_DAYS -> "Log health data 3 days in a row"
    AchievementType.STREAK_7_DAYS -> "Log health data 7 days in a row"
    AchievementType.STREAK_30_DAYS -> "Log health data 30 days in a row"
    AchievementType.STREAK_100_DAYS -> "Log health data 100 days in a row"
    AchievementType.FIRST_SYMPTOM -> "Record your first symptom"
    AchievementType.FIRST_VITAL -> "Record your first vital sign"
    AchievementType.FIRST_MEDICATION -> "Add your first medication"
    AchievementType.LOG_10_SYMPTOMS -> "Log 10 symptoms"
    AchievementType.LOG_50_SYMPTOMS -> "Log 50 symptoms"
    AchievementType.LOG_100_VITALS -> "Log 100 vital signs"
    AchievementType.FIRST_AI_REPORT -> "Generate your first AI report"
    AchievementType.COMPLETE_PROFILE -> "Fill in all profile information"
    AchievementType.ADD_FAMILY_MEMBER -> "Add a family member"
    AchievementType.FIRST_APPOINTMENT -> "Schedule your first appointment"
    AchievementType.EXPORT_DATA -> "Export your health data"
    AchievementType.SEVEN_DAY_VITALS -> "Record vitals for 7 consecutive days"
}

private fun AchievementType.toIcon(): String = when (this) {
    AchievementType.STREAK_3_DAYS -> "local_fire_department"
    AchievementType.STREAK_7_DAYS -> "whatshot"
    AchievementType.STREAK_30_DAYS -> "military_tech"
    AchievementType.STREAK_100_DAYS -> "emoji_events"
    AchievementType.FIRST_SYMPTOM -> "edit_note"
    AchievementType.FIRST_VITAL -> "monitor_heart"
    AchievementType.FIRST_MEDICATION -> "medication"
    AchievementType.LOG_10_SYMPTOMS -> "format_list_numbered"
    AchievementType.LOG_50_SYMPTOMS -> "star"
    AchievementType.LOG_100_VITALS -> "trending_up"
    AchievementType.FIRST_AI_REPORT -> "psychology"
    AchievementType.COMPLETE_PROFILE -> "person"
    AchievementType.ADD_FAMILY_MEMBER -> "group"
    AchievementType.FIRST_APPOINTMENT -> "event"
    AchievementType.EXPORT_DATA -> "cloud_upload"
    AchievementType.SEVEN_DAY_VITALS -> "calendar_month"
}

private fun AchievementType.toMaxProgress(): Int = when (this) {
    AchievementType.STREAK_3_DAYS -> 3
    AchievementType.STREAK_7_DAYS -> 7
    AchievementType.STREAK_30_DAYS -> 30
    AchievementType.STREAK_100_DAYS -> 100
    AchievementType.LOG_10_SYMPTOMS -> 10
    AchievementType.LOG_50_SYMPTOMS -> 50
    AchievementType.LOG_100_VITALS -> 100
    AchievementType.SEVEN_DAY_VITALS -> 7
    else -> 1
}
