package com.oroclub.game.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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

/** Sound toggle and a confirmed progress reset. Nothing else is configurable. */
@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var confirmVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        OroScreen(title = "Settings", onBack = onBack) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 6.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(DeepRoyal, Panel)))
                        .border(1.dp, GoldDark, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sound effects",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (soundEnabled) "On" else "Off",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundChange,
                        modifier = Modifier.semantics {
                            contentDescription =
                                if (soundEnabled) "Sound effects on" else "Sound effects off"
                        },
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

                GoldDivider(modifier = Modifier.align(Alignment.CenterHorizontally))

                BodyText(
                    // Kept accurate on purpose: the game's own data really is local, but
                    // the app as a whole does use the network, and claiming otherwise on
                    // a screen that sits next to the privacy policy would be a lie.
                    text = "Your game progress — best quiz scores, cleared levels and this " +
                        "switch — is stored on this device only. There is no account.",
                    modifier = Modifier.fillMaxWidth()
                )

                PlateButton(
                    text = "POLICY",
                    onClick = { openUrl(context, POLICY_URL) },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally),
                    contentDescription = "Privacy policy, opens in your browser"
                )

                PlateButton(
                    text = "SUPPORT",
                    onClick = { openUrl(context, SUPPORT_URL) },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally),
                    contentDescription = "Support, opens in your browser"
                )

                PlateButton(
                    text = "RESET PROGRESS",
                    onClick = { confirmVisible = true },
                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
                )

                BodyText(
                    text = "Oro Club is a for-fun quiz and memory game. It offers no " +
                        "real-money play, no wagering and no prizes.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ModalPanelOverlay(visible = confirmVisible) {
            GoldTitle(text = "Reset progress?", style = MaterialTheme.typography.headlineSmall)
            GoldDivider()
            BodyText(text = "Best scores and cleared levels will be erased. This cannot be undone.")
            PlateButton(
                text = "YES, RESET",
                onClick = {
                    confirmVisible = false
                    onReset()
                },
                modifier = Modifier.fillMaxWidth(0.86f)
            )
            PlateButton(
                text = "CANCEL",
                onClick = { confirmVisible = false },
                modifier = Modifier.fillMaxWidth(0.86f)
            )
        }
    }
}

private const val POLICY_URL = "https://telegra.ph/Privacy-Policy--Oro-Club-09-02"
private const val SUPPORT_URL = "https://telegra.ph/Support-09-02-3"

/**
 * Opens a link in the user's browser rather than in the app.
 *
 * FLAG_ACTIVITY_NEW_TASK because the target lands in its own task, and the whole call is
 * fenced: a device with no browser at all would otherwise crash the settings screen on a
 * tap, and a dead link is a far smaller problem than that.
 */
private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.onFailure { Log.w("SettingsScreen", "Could not open $url", it) }
}
