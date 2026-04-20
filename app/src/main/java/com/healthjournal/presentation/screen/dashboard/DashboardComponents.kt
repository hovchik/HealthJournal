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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.healthjournal.R
import com.healthjournal.presentation.theme.LocalHealthExtended
import java.time.LocalTime

/*
 * Editorial primitives. No Surface, no BorderStroke — every piece of chrome
 * here is text + a hairline rule. Callers compose these into full-bleed
 * columns separated by negative space and rules.
 */

// Outer margin used throughout the dashboard. The grid "starts" here.
val EditorialMargin = 20.dp

// ── Horizontal rules ─────────────────────────────────────────────────────────
// The three weights carry meaning:
//   • 0.5dp  — within a section, between rows of equivalent items
//   • 1dp    — under headline text / between sections
//   • 2dp    — above a new major section header (strongest break)
@Composable
fun HairlineRule(
    modifier: Modifier = Modifier,
    weight: Float = 0.5f
) {
    val ext = LocalHealthExtended.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(weight.dp)
            .background(ext.hairline)
    )
}

@Composable
fun InkRule(modifier: Modifier = Modifier, weight: Float = 1f) {
    val ext = LocalHealthExtended.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(weight.dp)
            .background(ext.ink)
    )
}

// ── Section header ───────────────────────────────────────────────────────────
// Replaces every "card" in the dashboard with ruled type. A thin ink bar on
// the left acts as a running head; the uppercase Grotesque label is the only
// remaining marker of "what's this section".
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    Column(modifier = modifier.fillMaxWidth()) {
        InkRule(weight = 1f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorialMargin, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 2.dp)
                    .background(ext.ink)
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = ext.ink
            )
        }
    }
}

// ── Hero ─────────────────────────────────────────────────────────────────────
// Flat. Not a container. Greeting in italic serif, title in display Grotesque,
// profile as a metadata line. The 2dp rule underneath closes the hero off
// like a magazine masthead.
@Composable
fun EditorialHero(
    profileName: String,
    isTopLevel: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    pulse: @Composable (Modifier) -> Unit = {}
) {
    val greeting = greetingForNow()
    val ext = LocalHealthExtended.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ext.paper)
    ) {
        if (!isTopLevel) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = ext.ink
                )
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorialMargin)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = ext.muted
            )
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.displaySmall,
                color = ext.ink
            )
            Spacer(Modifier.height(6.dp))
            // Metadata line: monospaced tabular marker dot + serif label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ext.signal)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.recording_for, profileName),
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.muted
                )
            }
            Spacer(Modifier.height(16.dp))

            // Ambient 14-day pulse. Runs edge-to-edge within the margin; drawn
            // by the screen so the hero stays dumb.
            pulse(Modifier.fillMaxWidth().height(56.dp))

            Spacer(Modifier.height(12.dp))
        }

        InkRule(weight = 2f)
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

// ── Period pill ──────────────────────────────────────────────────────────────
// The first of two allowed pill radii. Segments are bare text with animated
// ink fill on the selected one — no container around the control itself.
@Composable
fun PeriodPill(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EditorialMargin),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { days ->
            val isSelected = days == selected
            val bg by animateColorAsState(
                if (isSelected) ext.ink else Color.Transparent,
                tween(220),
                label = "pillBg"
            )
            val fg by animateColorAsState(
                if (isSelected) ext.paper else ext.muted,
                tween(220),
                label = "pillFg"
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .clickable { onSelect(days) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.days_format, days),
                    style = MaterialTheme.typography.labelMedium,
                    color = fg
                )
            }
        }
    }
}

// ── Baseline reading ─────────────────────────────────────────────────────────
// Structural text block used for stat + context pairs. The reading sits at
// the baseline with its serif caption to the right on the same line — the
// alignment IS the grouping; there's no box.
@Composable
fun BaselineReading(
    value: String,
    caption: String,
    accent: Color? = null,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EditorialMargin, vertical = 14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = ext.numerals),
            color = accent ?: ext.ink
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = ext.muted,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────
@Composable
fun EditorialEmpty(modifier: Modifier = Modifier) {
    val ext = LocalHealthExtended.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EditorialMargin, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = ext.ink
        )
        Text(
            text = stringResource(R.string.dashboard_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = ext.muted
        )
    }
}
