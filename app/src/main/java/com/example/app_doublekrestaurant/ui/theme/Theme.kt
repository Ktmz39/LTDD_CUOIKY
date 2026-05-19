package com.example.app_doublekrestaurant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors (DoubleK Restaurant)
val BrandRed = Color(0xFFAC2D00)
val BrandRedLight = Color(0xFFFF7043)
val BrandDark = Color(0xFF1A1C1E)
val BrandGray = Color(0xFF2C2F33)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0E6E6),
    onPrimaryContainer = BrandRed,
    secondary = BrandDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = BrandDark,
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFFF5F5F5),
    onBackground = BrandDark,
    surface = Color.White,
    onSurface = BrandDark,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
    error = Color(0xFFB00020),
    outline = Color(0xFFE0E0E0)
)

@Composable
fun DoubleKRestaurantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}