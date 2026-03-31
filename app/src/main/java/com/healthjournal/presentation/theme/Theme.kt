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
// Vibrant medical-teal palette for a premium, trustworthy feel
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),           // Teal 700 — signature health-app teal
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),  // Light cyan-teal
    onPrimaryContainer = Color(0xFF002B26),
    secondary = Color(0xFF388E3C),         // Fresh green for positive indicators
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF00210B),
    tertiary = Color(0xFF1565C0),          // Sapphire blue — vitals & data
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF001547),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5FAF8),        // Barely-tinted white for cleanliness
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBEC9C5),
    inverseSurface = Color(0xFF2B3230),
    inverseOnSurface = Color(0xFFEDF2EF),
    inversePrimary = Color(0xFF80CBC4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F2),
    surfaceContainer = Color(0xFFE9EFEC),
    surfaceContainerHigh = Color(0xFFE3E9E6),
    surfaceContainerHighest = Color(0xFFDEE4E1)
)

// ── Dark Scheme ───────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),           // Teal 200
    onPrimary = Color(0xFF00372F),
    primaryContainer = Color(0xFF004D46),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFFA5D6A7),         // Green 200
    onSecondary = Color(0xFF003909),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF90CAF9),          // Blue 200
    onTertiary = Color(0xFF00338A),
    tertiaryContainer = Color(0xFF0D47A1),
    onTertiaryContainer = Color(0xFFBBDEFB),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDEE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDEE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    inverseSurface = Color(0xFFDEE4E1),
    inverseOnSurface = Color(0xFF2B3230),
    inversePrimary = Color(0xFF00796B),
    surfaceContainerLowest = Color(0xFF090F0D),
    surfaceContainerLow = Color(0xFF171D1B),
    surfaceContainer = Color(0xFF1B211F),
    surfaceContainerHigh = Color(0xFF262C2A),
    surfaceContainerHighest = Color(0xFF303634)
)

// ── AMOLED Scheme ─────────────────────────────────────────────────────────────
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00372F),
    primaryContainer = Color(0xFF1A3532),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF003909),
    secondaryContainer = Color(0xFF1A2E1B),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF90CAF9),
    onTertiary = Color(0xFF00338A),
    tertiaryContainer = Color(0xFF0D2352),
    onTertiaryContainer = Color(0xFFBBDEFB),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF350003),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFDEE4E1),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFDEE4E1),
    surfaceVariant = Color(0xFF1E2A28),
    onSurfaceVariant = Color(0xFFBEC9C5),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF232D2B),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0F0D),
    surfaceContainer = Color(0xFF0F1513),
    surfaceContainerHigh = Color(0xFF1A2020),
    surfaceContainerHighest = Color(0xFF222929)
)

// ── Typography ────────────────────────────────────────────────────────────────
// Tighter, more modern spacing with better weight hierarchy
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
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
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
