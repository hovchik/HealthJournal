package com.healthjournal.presentation.screen.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthjournal.R
import com.healthjournal.presentation.theme.LocalHealthExtended
import java.time.LocalTime

// ── Collapsing hero ───────────────────────────────────────────────────────────
// Replaces the small TopAppBar. Greeting + active profile sit on top of an
// ambient gradient that fades into the background. The pulse sparkline is
// rendered separately by the screen so the header stays layout-cheap.
@Composable
fun DashboardHero(
    profileName: String,
    isTopLevel: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    pulse: @Composable (Modifier) -> Unit = {}
) {
    val greeting = greetingForNow()
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 188.dp)
            .background(
                Brush.verticalGradient(
                    0f to cs.surfaceContainer,
                    1f to cs.background
                )
            )
    ) {
        // Ambient sparkline lives behind the text
        pulse(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
        ) {
            if (!isTopLevel) {
                IconButton(onClick = onBack, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = cs.onSurfaceVariant
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = greeting,
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineLarge,
                color = cs.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = CircleShape,
                color = cs.surfaceContainerLow,
                contentColor = cs.onSurfaceVariant
            ) {
                Text(
                    text = stringResource(R.string.recording_for, profileName),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun greetingForNow(): String {
    val hour = LocalTime.now().hour
    val resId = when (hour) {
        in 5..11 -> R.string.dashboard_greeting_morning
        in 12..17 -> R.string.dashboard_greeting_day
        in 18..22 -> R.string.dashboard_greeting_evening
        else -> R.string.dashboard_greeting_night
    }
    return stringResource(resId)
}

// ── Period segmented control ──────────────────────────────────────────────────
// Replaces the FilterChip row. Pill segments animate the selection background.
@Composable
fun PeriodSegmentedControl(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cs.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEach { days ->
                val isSelected = days == selected
                val bg by animateColorAsState(
                    if (isSelected) cs.surface else Color.Transparent,
                    animationSpec = tween(220),
                    label = "segBg"
                )
                val fg by animateColorAsState(
                    if (isSelected) cs.onSurface else cs.onSurfaceVariant,
                    animationSpec = tween(220),
                    label = "segFg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .clickable { onSelect(days) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.days_format, days),
                        style = MaterialTheme.typography.labelLarge,
                        color = fg
                    )
                }
            }
        }
    }
}

// ── Stat tile ─────────────────────────────────────────────────────────────────
// 1×1 bento tile: hairline outline, no shadow. Number rendered in the
// monospace numeral family so digits sit on a stable grid.
@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = cs.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ext.gridInk),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = ext.numerals),
                color = cs.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant
            )
        }
    }
}

// ── Section divider label ─────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}
