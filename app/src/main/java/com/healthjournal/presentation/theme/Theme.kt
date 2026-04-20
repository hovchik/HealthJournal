package com.healthjournal.presentation.theme

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Type families ─────────────────────────────────────────────────────────────
// Pragmatic choice: system families ship with every Android device and give
// the editorial / neutral / monospace contrast the redesign calls for without
// adding a runtime font-provider dependency.
//   • Serif    → Noto Serif (Fraunces stand-in) — editorial display
//   • SansSerif → Roboto / Noto Sans            — neutral UI
//   • Monospace → Cousine / Droid Sans Mono     — tabular numerals
private val DisplayFamily = FontFamily.Serif
private val UiFamily = FontFamily.SansSerif
val NumeralFamily = FontFamily.Monospace

// ── Light Scheme ─────────────────────────────────────────────────────────────
// "Clinical Calm": warm paper background, deep teal ink, terracotta as the
// single signal accent. Periwinkle reserved for data ranges.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0B5E57),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2A),
    secondary = Color(0xFFE07856),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDEADF),
    onSecondaryContainer = Color(0xFF43200C),
    tertiary = Color(0xFF5B61E5),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E1FB),
    onTertiaryContainer = Color(0xFF0E1259),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF450A0A),
    background = Color(0xFFF7F5F1),
    onBackground = Color(0xFF1F2937),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFEFEAE2),
    onSurfaceVariant = Color(0xFF4B5560),
    outline = Color(0xFFC9C2B5),
    outlineVariant = Color(0xFFE2DCD0),
    inverseSurface = Color(0xFF222A27),
    inverseOnSurface = Color(0xFFF0F4F1),
    inversePrimary = Color(0xFF5EEAD4),
    surfaceTint = Color(0xFF0B5E57),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2EEE7),
    surfaceContainer = Color(0xFFEFEAE2),
    surfaceContainerHigh = Color(0xFFE8E2D7),
    surfaceContainerHighest = Color(0xFFE0DACC)
)

// ── Dark Scheme ───────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF053A34),
    primaryContainer = Color(0xFF0E4945),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFFFCA789),
    onSecondary = Color(0xFF4D1F0A),
    secondaryContainer = Color(0xFF6B2E14),
    onSecondaryContainer = Color(0xFFFDEADF),
    tertiary = Color(0xFFA5A8F5),
    onTertiary = Color(0xFF1A1E68),
    tertiaryContainer = Color(0xFF2E2F8C),
    onTertiaryContainer = Color(0xFFE0E1FB),
    error = Color(0xFFF87171),
    onError = Color(0xFF5A0A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0A1110),
    onBackground = Color(0xFFE5EAE7),
    surface = Color(0xFF121A18),
    onSurface = Color(0xFFE5EAE7),
    surfaceVariant = Color(0xFF2A3431),
    onSurfaceVariant = Color(0xFFB6BFBB),
    outline = Color(0xFF7E8783),
    outlineVariant = Color(0xFF2E3A37),
    inverseSurface = Color(0xFFE5EAE7),
    inverseOnSurface = Color(0xFF222A27),
    inversePrimary = Color(0xFF0B5E57),
    surfaceTint = Color(0xFF5EEAD4),
    surfaceContainerLowest = Color(0xFF050908),
    surfaceContainerLow = Color(0xFF101715),
    surfaceContainer = Color(0xFF161E1C),
    surfaceContainerHigh = Color(0xFF1E2624),
    surfaceContainerHighest = Color(0xFF272F2C)
)

// ── AMOLED Scheme ─────────────────────────────────────────────────────────────
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF053A34),
    primaryContainer = Color(0xFF0A2825),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFFFCA789),
    onSecondary = Color(0xFF4D1F0A),
    secondaryContainer = Color(0xFF2A160A),
    onSecondaryContainer = Color(0xFFFDEADF),
    tertiary = Color(0xFFA5A8F5),
    onTertiary = Color(0xFF1A1E68),
    tertiaryContainer = Color(0xFF161847),
    onTertiaryContainer = Color(0xFFE0E1FB),
    error = Color(0xFFF87171),
    onError = Color(0xFF5A0A0A),
    errorContainer = Color(0xFF2E0A0A),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE5EAE7),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE5EAE7),
    surfaceVariant = Color(0xFF1A2421),
    onSurfaceVariant = Color(0xFFB6BFBB),
    outline = Color(0xFF7E8783),
    outlineVariant = Color(0xFF1A2421),
    surfaceTint = Color(0xFF5EEAD4),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080C0B),
    surfaceContainer = Color(0xFF0C1210),
    surfaceContainerHigh = Color(0xFF131A18),
    surfaceContainerHighest = Color(0xFF1B2421)
)

// ── Typography ────────────────────────────────────────────────────────────────
// Display / headline → Serif (editorial). UI / body → SansSerif. Numerals →
// Monospace via the NumeralFamily token applied at the call site.
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 56.sp,
        lineHeight = 62.sp,
        letterSpacing = (-1.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.75).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.15).sp
    ),
    titleMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)

// ── Shapes ────────────────────────────────────────────────────────────────────
// Tighter than the previous iteration: clinical, not childish.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Extended tokens not covered by Material's ColorScheme.
data class HealthExtendedColors(
    val signal: Color,
    val onSignal: Color,
    val signalContainer: Color,
    val data: Color,
    val gridInk: Color,
    val numerals: FontFamily
)

val LocalHealthExtended = compositionLocalOf {
    HealthExtendedColors(
        signal = Color(0xFFE07856),
        onSignal = Color.White,
        signalContainer = Color(0xFFFDEADF),
        data = Color(0xFF5B61E5),
        gridInk = Color(0xFFC9C2B5),
        numerals = NumeralFamily
    )
}

// ── Theme entry point ─────────────────────────────────────────────────────────
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

    val extended = HealthExtendedColors(
        signal = if (darkTheme) Color(0xFFFCA789) else Color(0xFFE07856),
        onSignal = if (darkTheme) Color(0xFF4D1F0A) else Color.White,
        signalContainer = if (darkTheme) Color(0xFF3B1A0A) else Color(0xFFFDEADF),
        data = if (darkTheme) Color(0xFFA5A8F5) else Color(0xFF5B61E5),
        gridInk = colorScheme.outlineVariant,
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
