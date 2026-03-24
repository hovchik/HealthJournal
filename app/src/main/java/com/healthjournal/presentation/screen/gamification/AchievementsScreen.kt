package com.healthjournal.presentation.screen.gamification

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.domain.model.Achievement
import com.healthjournal.domain.model.AchievementType
import com.healthjournal.domain.model.GamificationStats
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.achievements_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats header
            item {
                StatsCard(stats)
            }

            // Achievements
            item {
                Text(
                    stringResource(R.string.achievements_list),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(achievements) { achievement ->
                AchievementCard(achievement)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatsCard(stats: GamificationStats) {
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${stats.currentStreak}",
                    label = stringResource(R.string.streak_current),
                    icon = Icons.Default.LocalFireDepartment,
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    value = "${stats.longestStreak}",
                    label = stringResource(R.string.streak_longest),
                    icon = Icons.Default.EmojiEvents,
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    value = "${stats.totalCheckIns}",
                    label = stringResource(R.string.total_checkins),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = "${stats.achievementsUnlocked}/${stats.totalAchievements}",
                    label = stringResource(R.string.achievements_count),
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val alpha = if (achievement.isUnlocked) 1f else 0.5f
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val progress by animateFloatAsState(targetValue = achievement.progressPercent, label = "progress")

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (achievement.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getAchievementIcon(achievement.type),
                        contentDescription = null,
                        tint = if (achievement.isUnlocked)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
                if (achievement.maxProgress > 1 && !achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                    )
                }
                if (achievement.isUnlocked && achievement.unlockedAt != null) {
                    Text(
                        stringResource(R.string.achievement_unlocked_at, achievement.unlockedAt.format(dateFormatter)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun getAchievementIcon(type: AchievementType): ImageVector = when (type) {
    AchievementType.STREAK_3_DAYS, AchievementType.STREAK_7_DAYS -> Icons.Default.LocalFireDepartment
    AchievementType.STREAK_30_DAYS -> Icons.Default.MilitaryTech
    AchievementType.STREAK_100_DAYS -> Icons.Default.EmojiEvents
    AchievementType.FIRST_SYMPTOM -> Icons.Default.EditNote
    AchievementType.FIRST_VITAL -> Icons.Default.MonitorHeart
    AchievementType.FIRST_MEDICATION -> Icons.Default.Medication
    AchievementType.LOG_10_SYMPTOMS, AchievementType.LOG_50_SYMPTOMS -> Icons.Default.FormatListNumbered
    AchievementType.LOG_100_VITALS -> Icons.Default.TrendingUp
    AchievementType.FIRST_AI_REPORT -> Icons.Default.Psychology
    AchievementType.COMPLETE_PROFILE -> Icons.Default.Person
    AchievementType.ADD_FAMILY_MEMBER -> Icons.Default.Group
    AchievementType.FIRST_APPOINTMENT -> Icons.Default.Event
    AchievementType.EXPORT_DATA -> Icons.Default.CloudUpload
    AchievementType.SEVEN_DAY_VITALS -> Icons.Default.CalendarMonth
}
