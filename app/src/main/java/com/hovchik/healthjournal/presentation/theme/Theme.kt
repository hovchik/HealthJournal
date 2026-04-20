package com.hovchik.healthjournal.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Editorial / Case-sheet design system ─────────────────────────────────────
// Rules instead of cards. Baselines instead of borders. Monochrome ink with
// one reactive signal color. Two radii exist in the whole app:
//   • 0.dp  — default for every surface. Unfloaten the dashboard.
//   • pill  — only applied ad-hoc to the segmented period control and the FAB.
// No gentle in-between radii. The absence is the statement.

// Proxies for Space Grotesk (geometric sans, tabular) + Newsreader (serif
// body). System families ship on every device and render reliably without a
// runtime font-provider dependency. Character is carried by weight, tracking,
// and scale — not by the glyph shapes alone.
private val Grotesque = FontFamily.SansSerif   // ≈ Space Grotesk
private val Serif = FontFamily.Serif           // ≈ Newsreader
val NumeralFamily = FontFamily.Monospace       // tabular figures for readings

// ── Light scheme: monochrome ink on paper ────────────────────────────────────
// Almost everything collapses to onBackground/background/outlineVariant.
// The only colorful tokens are the reserved chart series (data-a/data-b) and
// the terracotta signal, used sparingly.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F1614),            // INK — all text, all icons, all rules
    onPrimary = Color(0xFFF7F5F1),
    primaryContainer = Color(0xFFE6E0D0),
    onPrimaryContainer = Color(0xFF0F1614),
    secondary = Color(0xFFE07856),          // SIGNAL — terracotta, reactive only
    onSecondary = Color(0xFFF7F5F1),
    secondaryContainer = Color(0xFFF6DACB),
    onSecondaryContainer = Color(0xFF3F1A0B),
    tertiary = Color(0xFF5B61E5),           // DATA-A — charts only
    onTertiary = Color(0xFFF7F5F1),
    tertiaryContainer = Color(0xFFDCDEFA),
    onTertiaryContainer = Color(0xFF0E1259),
    error = Color(0xFFB42318),
    onError = Color(0xFFF7F5F1),
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF450A0A),
    background = Color(0xFFF7F5F1),         // PAPER — the one and only surface
    onBackground = Color(0xFF0F1614),
    surface = Color(0xFFF7F5F1),
    onSurface = Color(0xFF0F1614),
    surfaceVariant = Color(0xFFEFEAE2),
    onSurfaceVariant = Color(0xFF8A9591),   // MUTED — secondary text, dates
    outline = Color(0xFF8A9591),
    outlineVariant = Color(0xFFDDD6C8),     // HAIRLINE — 0.5dp rules
    inverseSurface = Color(0xFF1E2624),
    inverseOnSurface = Color(0xFFF0ECE1),
    inversePrimary = Color(0xFFE6E0D0),
    surfaceTint = Color(0xFF0F1614),
    surfaceContainerLowest = Color(0xFFF7F5F1),
    surfaceContainerLow = Color(0xFFF2EDE3),
    surfaceContainer = Color(0xFFEEE8DB),
    surfaceContainerHigh = Color(0xFFE8E1D1),
    surfaceContainerHighest = Color(0xFFE0D8C4)
)

// ── Dark scheme: ink palette inverted ────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5EAE7),
    onPrimary = Color(0xFF0A1110),
    primaryContainer = Color(0xFF1C2523),
    onPrimaryContainer = Color(0xFFE5EAE7),
    secondary = Color(0xFFFCA789),
    onSecondary = Color(0xFF3F1A0B),
    secondaryContainer = Color(0xFF5F2D18),
    onSecondaryContainer = Color(0xFFFDEADF),
    tertiary = Color(0xFFA5A8F5),
    onTertiary = Color(0xFF1A1E68),
    tertiaryContainer = Color(0xFF2E2F8C),
    onTertiaryContainer = Color(0xFFE0E1FB),
    error = Color(0xFFF87171),
    onError = Color(0xFF5A0A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0A1110),         // near-black paper
    onBackground = Color(0xFFE5EAE7),
    surface = Color(0xFF0A1110),
    onSurface = Color(0xFFE5EAE7),
    surfaceVariant = Color(0xFF1C2523),
    onSurfaceVariant = Color(0xFF8A9591),
    outline = Color(0xFF8A9591),
    outlineVariant = Color(0xFF2E3A37),
    inverseSurface = Color(0xFFE5EAE7),
    inverseOnSurface = Color(0xFF0A1110),
    inversePrimary = Color(0xFF0F1614),
    surfaceTint = Color(0xFFE5EAE7),
    surfaceContainerLowest = Color(0xFF050908),
    surfaceContainerLow = Color(0xFF101715),
    surfaceContainer = Color(0xFF161E1C),
    surfaceContainerHigh = Color(0xFF1E2624),
    surfaceContainerHighest = Color(0xFF272F2C)
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0E0D),
    surfaceContainer = Color(0xFF0F1413),
    surfaceContainerHigh = Color(0xFF161C1A),
    surfaceContainerHighest = Color(0xFF1F2624)
)

// ── Typography ───────────────────────────────────────────────────────────────
// Display scale is aggressive so readings can carry the dashboard without
// surrounding chrome. Section headers use tight-tracked uppercase Grotesque —
// they *become* the divider with a companion hairline rule, not with a box.
private val AppTypography = Typography(
    // Page titles and hero numbers. 2% negative tracking mimics display-optical
    // sizes and keeps big Grotesque from feeling airy.
    displayLarge = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1.6).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp
    ),
    // Screen titles — Serif, to counterweight the technical numerals.
    headlineLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    // Title / body — Serif (Newsreader proxy) for a print-journal texture.
    titleLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        fontStyle = FontStyle.Italic    // captions read as editorial asides
    ),
    // Grotesque uppercase labels. Used as section rules — paired with a
    // hairline they replace every "card header" in the app.
    labelLarge = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Grotesque,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)

// ── Shapes: the two-radius rule ──────────────────────────────────────────────
// Everything that reads Material-shape-token gets 0dp. Components that want
// the pill treatment use CircleShape ad-hoc at the call site — so the rule is
// visible in code review, not hidden behind a token name.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

// Extended tokens surfaced via CompositionLocal.
data class HealthExtendedColors(
    val ink: Color,             // primary text / rules / icons
    val paper: Color,           // background
    val muted: Color,           // secondary text, dates, metadata
    val hairline: Color,        // 0.5dp rules
    val signal: Color,          // terracotta — reactive state ONLY
    val onSignal: Color,
    val dataA: Color,           // chart series A
    val dataB: Color,           // chart series B
    val numerals: FontFamily
)

val LocalHealthExtended = compositionLocalOf {
    HealthExtendedColors(
        ink = Color(0xFF0F1614),
        paper = Color(0xFFF7F5F1),
        muted = Color(0xFF8A9591),
        hairline = Color(0xFFDDD6C8),
        signal = Color(0xFFE07856),
        onSignal = Color(0xFFF7F5F1),
        dataA = Color(0xFF5B61E5),
        dataB = Color(0xFF0B5E57),
        numerals = NumeralFamily
    )
}

@Composable
fun HealthJournalTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val isAmoled = themeMode == "AMOLED"
    val darkTheme = when (themeMode) {
        "DARK", "AMOLED" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isAmoled -> AmoledColorScheme
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extended = if (darkTheme) HealthExtendedColors(
        ink = Color(0xFFE5EAE7),
        paper = Color(0xFF0A1110),
        muted = Color(0xFF8A9591),
        hairline = Color(0xFF2E3A37),
        signal = Color(0xFFFCA789),
        onSignal = Color(0xFF3F1A0B),
        dataA = Color(0xFFA5A8F5),
        dataB = Color(0xFF5EEAD4),
        numerals = NumeralFamily
    ) else HealthExtendedColors(
        ink = Color(0xFF0F1614),
        paper = Color(0xFFF7F5F1),
        muted = Color(0xFF8A9591),
        hairline = Color(0xFFDDD6C8),
        signal = Color(0xFFE07856),
        onSignal = Color(0xFFF7F5F1),
        dataA = Color(0xFF5B61E5),
        dataB = Color(0xFF0B5E57),
        numerals = NumeralFamily
    )

    CompositionLocalProvider(LocalHealthExtended provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
