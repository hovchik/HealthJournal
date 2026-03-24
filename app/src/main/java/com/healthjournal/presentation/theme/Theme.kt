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

// Vibrant, modern health-themed palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D6A4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E4C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF40916C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F3DC),
    onSecondaryContainer = Color(0xFF0B2618),
    tertiary = Color(0xFF1B4965),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBDE0FE),
    onTertiaryContainer = Color(0xFF001D30),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFF8FBF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF8FBF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DB),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC1C9BF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5F0),
    surfaceContainer = Color(0xFFECEFEA),
    surfaceContainerHigh = Color(0xFFE6EAE5),
    surfaceContainerHighest = Color(0xFFE1E4DF)
)

// AMOLED pure black theme
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF95D5B2),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF1B5139),
    onPrimaryContainer = Color(0xFFB7E4C7),
    secondary = Color(0xFFA7D7C5),
    onSecondary = Color(0xFF11372A),
    secondaryContainer = Color(0xFF295240),
    onSecondaryContainer = Color(0xFFD8F3DC),
    tertiary = Color(0xFF89CFF0),
    onTertiary = Color(0xFF003350),
    tertiaryContainer = Color(0xFF0D3B5E),
    onTertiaryContainer = Color(0xFFBDE0FE),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE1E4DF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE1E4DF),
    surfaceVariant = Color(0xFF2A2D2A),
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF8B938A),
    outlineVariant = Color(0xFF2A2D2A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0C0A),
    surfaceContainer = Color(0xFF111311),
    surfaceContainerHigh = Color(0xFF1A1C1A),
    surfaceContainerHighest = Color(0xFF222422)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF95D5B2),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF1B5139),
    onPrimaryContainer = Color(0xFFB7E4C7),
    secondary = Color(0xFFA7D7C5),
    onSecondary = Color(0xFF11372A),
    secondaryContainer = Color(0xFF295240),
    onSecondaryContainer = Color(0xFFD8F3DC),
    tertiary = Color(0xFF89CFF0),
    onTertiary = Color(0xFF003350),
    tertiaryContainer = Color(0xFF0D3B5E),
    onTertiaryContainer = Color(0xFFBDE0FE),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE1E4DF),
    surface = Color(0xFF101412),
    onSurface = Color(0xFFE1E4DF),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9BF),
    outline = Color(0xFF8B938A),
    outlineVariant = Color(0xFF414942),
    surfaceContainerLowest = Color(0xFF0B0F0C),
    surfaceContainerLow = Color(0xFF191C1A),
    surfaceContainer = Color(0xFF1D211E),
    surfaceContainerHigh = Color(0xFF282B28),
    surfaceContainerHighest = Color(0xFF323633)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.15).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
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
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

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
