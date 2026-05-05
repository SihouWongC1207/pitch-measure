package com.pitchmeasure.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    secondary = Blue400,
    tertiary = Green500,
    background = GrayBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    secondary = Blue500,
    tertiary = Green500,
)

@Composable
fun PitchMeasureTheme(
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
