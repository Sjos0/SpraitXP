package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RetroDarkColorScheme = darkColorScheme(
    primary = LunaTitleMid,
    onPrimary = XpTextWhite,
    primaryContainer = LunaTitleStart,
    onPrimaryContainer = XpTextWhite,
    secondary = XpStartGreen,
    onSecondary = XpTextWhite,
    background = XpDarkWindowBg,
    onBackground = XpDarkText,
    surface = XpDarkWindowBg,
    onSurface = XpDarkText,
    surfaceVariant = XpDarkInnerDark,
    onSurfaceVariant = XpDarkTextMuted,
    error = XpCloseOrange,
    onError = XpTextWhite
)

private val RetroLightColorScheme = lightColorScheme(
    primary = LunaTitleMid,
    onPrimary = XpTextWhite,
    primaryContainer = LunaTitleStart,
    onPrimaryContainer = XpTextWhite,
    secondary = XpStartGreen,
    onSecondary = XpTextWhite,
    background = XpWindowBg,
    onBackground = XpTextBlack,
    surface = XpWindowBg,
    onSurface = XpTextBlack,
    surfaceVariant = XpInnerBorderDark,
    onSurfaceVariant = XpTextMuted,
    error = XpCloseOrange,
    onError = XpTextWhite
)

@Composable
fun SpriteXPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RetroDarkColorScheme else RetroLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LunaTitleStart.toArgb()
            window.navigationBarColor = XpTaskbarDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RetroTypography,
        content = content
    )
}
