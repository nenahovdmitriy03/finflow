package com.finflow.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColors = lightColorScheme(
    primary = Primary, onPrimary = Color.White, primaryContainer = PrimaryVariant,
    secondary = Income, background = BackgroundLight, surface = SurfaceLight,
    onBackground = OnSurfaceLight, onSurface = OnSurfaceLight, error = Expense,
)

private val DarkColors = darkColorScheme(
    primary = Primary, onPrimary = Color.White, primaryContainer = PrimaryVariant,
    secondary = Income, background = BackgroundDark, surface = SurfaceDark,
    onBackground = OnSurfaceDark, onSurface = OnSurfaceDark, error = Expense,
)

@Composable
fun FinFlowTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FinFlowTypography,
        content = content,
    )
}
