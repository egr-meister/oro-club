package com.oroclub.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Theme choice offered on the Settings screen. Persisted as [storageKey]. */
enum class ThemeMode(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(key: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: DARK
    }
}

private val OroDarkScheme = darkColorScheme(
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
 * A light scheme so the theme toggle is real. Oro Club's artwork is a fixed night-sky
 * background, so light mode mainly re-tints Material surfaces rather than repainting the
 * whole screen; the background image stays dark by design.
 */
private val OroLightScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = TextPrimary,
    primaryContainer = GoldSoft,
    onPrimaryContainer = NearBlack,
    secondary = Gem,
    onSecondary = NearBlack,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = WrongRed,
    onError = TextPrimary,
    outline = GoldDark
)

@Composable
fun OroClubTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) OroDarkScheme else OroLightScheme,
        typography = OroTypography,
        content = content
    )
}
