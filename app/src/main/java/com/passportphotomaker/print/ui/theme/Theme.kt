package com.passportphotomaker.print.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = Color.White,
    secondary = BrandIndigo,
    onSecondary = Color.White,
    tertiary = BrandCyan,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE6EEFF),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFB9C7E0),
    error = ErrorRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = Color(0xFF06122B),
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandCyan,
    onSecondary = Color(0xFF04121F),
    tertiary = BrandIndigo,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1E2940),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF3A4864),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A0505)
)

@Composable
fun PassportPhotoMakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
