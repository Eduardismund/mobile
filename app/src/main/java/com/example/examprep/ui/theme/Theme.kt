package com.example.examprep.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// App Color Palette
val Primary = Color(0xFF2196F3)        // Blue
val PrimaryDark = Color(0xFF1976D2)    // Darker Blue
val Accent = Color(0xFF00BCD4)         // Cyan
val StatusOngoing = Color(0xFF4CAF50)  // Green
val StatusUpcoming = Color(0xFFFF9800) // Orange
val StatusCompleted = Color(0xFF9E9E9E)// Gray
val RankGold = Color(0xFFFFD700)       // Gold
val RankSilver = Color(0xFFC0C0C0)     // Silver
val RankBronze = Color(0xFFCD7F32)     // Bronze
val ErrorRed = Color(0xFFE53935)       // Red
val TextPrimary = Color(0xFF212121)    // Dark Gray
val TextSecondary = Color(0xFF757575)  // Medium Gray
val Background = Color(0xFFFAFAFA)     // Light Gray Background
val CardBackground = Color.White       // White

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Accent,
    tertiary = PrimaryDark,
    background = Background,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun CourseManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
