package com.healthjournal.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Light Scheme ─────────────────────────────────────────────────────────────
// Modern wellness palette: calm sage/teal primary, warm terracotta accent,
// periwinkle for data. Breathes, feels human, still trustworthy.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F766E),           // Teal 700 — calm & trustworthy
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),  // Soft mint
    onPrimaryContainer = Color(0xFF042F2A),
    secondary = Color(0xFFD97757),         // Warm terracotta — human & energetic
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDEADF),
    onSecondaryContainer = Color(0xFF43200C),
    tertiary = Color(0xFF5B61E5),          // Periwinkle indigo — vitals & data
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0E1FB),
    onTertiaryContainer = Color(0xFF0E1259),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF450A0A),
    background = Color(0xFFFAFBF9),        // Warm off-white
    onBackground = Color(0xFF101614),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101614),
    surfaceVariant = Color(0xFFE2E8E4),
    onSurfaceVariant = Color(0xFF43484A),
    outline = Color(0xFF747A78),
    outlineVariant = Color(0xFFD3D9D6),
    inverseSurface = Color(0xFF252B29),
    inverseOnSurface = Color(0xFFF0F4F1),
    inversePrimary = Color(0xFF5EEAD4),
    surfaceTint = Color(0xFF0F766E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F6F4),
    surfaceContainer = Color(0xFFEDF1EE),
    surfaceContainerHigh = Color(0xFFE7ECE9),
    surfaceContainerHighest = Color(0xFFE1E6E3)
)

// ── Dark Scheme ───────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),           // Luminous teal
    onPrimary = Color(0xFF053A34),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFFFCA789),         // Warm coral glow
    onSecondary = Color(0xFF4D1F0A),
    secondaryContainer = Color(0xFF7C3A1D),
    onSecondaryContainer = Color(0xFFFDEADF),
    tertiary = Color(0xFFA5A8F5),          // Soft periwinkle
    onTertiary = Color(0xFF1A1E68),
    tertiaryContainer = Color(0xFF3730A3),
    onTertiaryContainer = Color(0xFFE0E1FB),
    error = Color(0xFFF87171),
    onError = Color(0xFF5A0A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0B1412),        // Deep slate-teal
    onBackground = Color(0xFFE1E6E3),
    surface = Color(0xFF0B1412),
    onSurface = Color(0xFFE1E6E3),
    surfaceVariant = Color(0xFF3A4542),
    onSurfaceVariant = Color(0xFFC3C9C6),
    outline = Color(0xFF8C9390),
    outlineVariant = Color(0xFF3A4542),
    inverseSurface = Color(0xFFE1E6E3),
    inverseOnSurface = Color(0xFF252B29),
    inversePrimary = Color(0xFF0F766E),
    surfaceTint = Color(0xFF5EEAD4),
    surfaceContainerLowest = Color(0xFF060C0B),
    surfaceContainerLow = Color(0xFF131A18),
    surfaceContainer = Color(0xFF171F1D),
    surfaceContainerHigh = Color(0xFF222A27),
    surfaceContainerHighest = Color(0xFF2C3532)
)

// ── AMOLED Scheme ─────────────────────────────────────────────────────────────
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF053A34),
    primaryContainer = Color(0xFF0E2E2A),
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
    onBackground = Color(0xFFE1E6E3),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE1E6E3),
    surfaceVariant = Color(0xFF1A2421),
    onSurfaceVariant = Color(0xFFC3C9C6),
    outline = Color(0xFF8C9390),
    outlineVariant = Color(0xFF1F2A27),
    surfaceTint = Color(0xFF5EEAD4),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF080C0B),
    surfaceContainer = Color(0xFF0C1210),
    surfaceContainerHigh = Color(0xFF161C1A),
    surfaceContainerHighest = Color(0xFF1F2522)
)

// ── Typography ────────────────────────────────────────────────────────────────
// Modern hierarchy with tighter display tracking and comfortable body line-height
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.15).sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

// ── Shapes ────────────────────────────────────────────────────────────────────
// Softer corners throughout for a friendlier, more modern feel
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
