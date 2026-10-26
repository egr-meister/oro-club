package com.oroclub.game.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.ui.LocaleManager
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.GoldDivider
import com.oroclub.game.ui.components.GoldTitle
import com.oroclub.game.ui.components.ModalPanelOverlay
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.components.PlateButton
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.NearBlack
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary
import com.oroclub.game.ui.theme.ThemeMode

/** Sound, theme, language, links and a confirmed progress reset. */
@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    onSoundChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var confirmVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        OroScreen(title = stringResource(R.string.settings_title), onBack = onBack) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 6.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Sound ---
                SettingCard {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_sound),
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(if (soundEnabled) R.string.settings_on else R.string.settings_off),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepRoyal,
                            checkedTrackColor = Gold,
                            checkedBorderColor = GoldDark,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = NearBlack,
                            uncheckedBorderColor = GoldDark
                        )
                    )
                }

                // --- Theme ---
                GoldTitle(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeChip(
                        label = stringResource(R.string.settings_theme_system),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeChange(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = stringResource(R.string.settings_theme_light),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeChange(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = stringResource(R.string.settings_theme_dark),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeChange(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- Language ---
                GoldTitle(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                val current = LocaleManager.currentLanguage()
                LanguageRow(
                    label = stringResource(R.string.settings_language_system),
                    selected = current == null,
                    onClick = { LocaleManager.applyLocale(null) }
                )
                LocaleManager.LANGUAGES.forEach { (tag, nativeName) ->
                    LanguageRow(
                        label = nativeName,
                        selected = current == tag,
                        onClick = { LocaleManager.applyLocale(tag) }
                    )
                }

                GoldDivider(modifier = Modifier.align(Alignment.CenterHorizontally))

                BodyText(
                    text = stringResource(R.string.settings_privacy_note),
                    modifier = Modifier.fillMaxWidth()
                )

                PlateButton(
                    text = stringResource(R.string.settings_policy),
                    onClick = { openUrl(context, POLICY_URL) },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally),
                    contentDescription = stringResource(R.string.a11y_policy)
                )
                PlateButton(
                    text = stringResource(R.string.settings_support),
                    onClick = { openUrl(context, SUPPORT_URL) },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally),
                    contentDescription = stringResource(R.string.a11y_support)
                )
                PlateButton(
                    text = stringResource(R.string.settings_reset),
                    onClick = { confirmVisible = true },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
                )

                BodyText(
                    text = stringResource(R.string.settings_fairplay_note),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ModalPanelOverlay(visible = confirmVisible) {
            GoldTitle(
                text = stringResource(R.string.settings_reset_confirm_title),
                style = MaterialTheme.typography.headlineSmall
            )
            GoldDivider()
            BodyText(text = stringResource(R.string.settings_reset_confirm_body))
            PlateButton(
                text = stringResource(R.string.settings_reset_yes),
                onClick = {
                    confirmVisible = false
                    onReset()
                },
                modifier = Modifier.fillMaxWidth(0.86f)
            )
            PlateButton(
                text = stringResource(R.string.settings_cancel),
                onClick = { confirmVisible = false },
                modifier = Modifier.fillMaxWidth(0.86f)
            )
        }
    }
}

@Composable
private fun SettingCard(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(DeepRoyal, Panel)))
            .border(1.dp, GoldDark, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Brush.horizontalGradient(listOf(GoldDark, Gold)) else Brush.horizontalGradient(listOf(DeepRoyal, Panel)))
            .border(1.dp, if (selected) Gold else GoldDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = label }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) DeepRoyal else TextPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Brush.horizontalGradient(listOf(DeepRoyal, Panel)) else Brush.horizontalGradient(listOf(NearBlack, DeepRoyal)))
            .border(1.dp, if (selected) Gold else GoldDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text(text = "✓", color = Gold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private const val POLICY_URL = "https://telegra.ph/Privacy-Policy--Oro-Club-09-02"
private const val SUPPORT_URL = "https://telegra.ph/Support-09-02-3"

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.onFailure { Log.w("SettingsScreen", "Could not open $url", it) }
}
