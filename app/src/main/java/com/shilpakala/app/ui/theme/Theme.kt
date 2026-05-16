package com.shilpakala.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors: ColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnPrimaryCream,
    primaryContainer = GoldSecondary.copy(alpha = 0.35f),
    secondary = GoldSecondary,
    onSecondary = DeepWoodTertiary,
    tertiary = DeepWoodTertiary,
    onTertiary = WarmCreamBackground,
    background = WarmCreamBackground,
    onBackground = DeepWoodTertiary,
    surface = SurfaceWhite,
    onSurface = DeepWoodTertiary,
    surfaceVariant = WarmCreamBackground.copy(alpha = 0.85f),
    onSurfaceVariant = DeepWoodTertiary.copy(alpha = 0.75f)
)

@Composable
fun ShilpaKalaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColors,
        typography = ShilpaKalaTypography,
        content = content
    )
}
