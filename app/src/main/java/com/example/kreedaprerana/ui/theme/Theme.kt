package com.example.kreedaprerana.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue800,
    secondary = Orange500,
    onSecondary = Color.White,
    secondaryContainer = Orange100,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = Success,
    onTertiary = Color.White,
    tertiaryContainer = SuccessLight,
    onTertiaryContainer = Color(0xFF064E3B),
    background = Background,
    onBackground = Slate900,
    surface = CardWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Error,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = Color(0xFF7F1D1D),
    inverseSurface = Slate900,
    inverseOnSurface = Slate50,
)

@Composable
fun KreedaPreranaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Blue700.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}