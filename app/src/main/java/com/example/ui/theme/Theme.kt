package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryNeonContainer,
    onPrimaryContainer = PrimaryNeon,
    secondary = SecondaryMint,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryMintContainer,
    onSecondaryContainer = SecondaryMint,
    tertiary = TertiaryViolet,
    background = BackgroundDark,
    onBackground = PrimaryText,
    surface = SurfaceDark,
    onSurface = PrimaryText,
    surfaceVariant = BorderColor,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007B8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC0EBF2),
    onPrimaryContainer = Color(0xFF002F3A),
    secondary = Color(0xFF008450),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC3FAD3),
    onSecondaryContainer = Color(0xFF00371D),
    tertiary = Color(0xFF8632C4),
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF151D26),
    surface = Color.White,
    onSurface = Color(0xFF151D26),
    surfaceVariant = Color(0xFFE2EBF5),
    onSurfaceVariant = Color(0xFF4C5D6F),
    outline = Color(0xFFCDD7E5),
    error = Color(0xFFBA1A1A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
