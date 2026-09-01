package com.oroclub.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OroColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = NearBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextPrimary,
    secondary = Gem,
    onSecondary = NearBlack,
    background = Ink,
    onBackground = TextPrimary,
    surface = Panel,
    onSurface = TextPrimary,
    surfaceVariant = DeepRoyal,
    onSurfaceVariant = TextMuted,
    error = WrongRed,
    onError = TextPrimary,
    outline = GoldDark
)

/**
 * Oro Club is a single dark, high-contrast theme by design — the artwork is a
 * night-sky background and a light theme would break every asset. Dynamic
 * colour is intentionally not used.
 */
@Composable
fun OroClubTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OroColorScheme,
        typography = OroTypography,
        content = content
    )
}
