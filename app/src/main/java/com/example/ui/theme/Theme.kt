package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ShinyGold,
    secondary = MediumPurple,
    tertiary = TagBlue,
    background = SoftCharcoal,
    surface = CardDark,
    onPrimary = SoftCharcoal,
    onSecondary = PureWhite,
    onBackground = GhostWhite,
    onSurface = GhostWhite
)

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    secondary = MediumPurple,
    tertiary = ShinyGold,
    background = GhostWhite,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = SoftCharcoal,
    onSurface = SoftCharcoal
)

@Composable
fun NoteFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
