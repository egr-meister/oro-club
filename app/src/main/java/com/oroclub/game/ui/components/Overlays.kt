package com.oroclub.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.ui.theme.ScrimColor

/**
 * A dimmed, tap-blocking scrim with a Compose-drawn [OroPanel] centred on it.
 * Every modal in the app (pause, results, rules) uses this one implementation,
 * so no decorative frame image is ever stretched around variable content.
 */
@Composable
fun ModalPanelOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    dismissOnScrimTap: Boolean = false,
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                // Swallows taps aimed at the board underneath.
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = true
                ) { if (dismissOnScrimTap) onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // The panel scrolls rather than clipping when the content is tall
            // or the system font scale is large.
            CenteredScrollContent {
                OroPanel(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 8.dp),
                    content = content
                )
            }
        }
    }
}

/**
 * The shared pause modal: Continue / Restart / Menu.
 * Buttons stay inside the panel padding by construction (fraction widths).
 */
@Composable
fun PauseOverlay(
    visible: Boolean,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    ModalPanelOverlay(visible = visible) {
        GoldTitle(text = stringResource(R.string.pause_title), style = MaterialTheme.typography.headlineMedium)
        GoldDivider()
        BodyText(text = stringResource(R.string.pause_body))
        PlateButton(text = stringResource(R.string.btn_continue), onClick = onContinue, modifier = Modifier.fillMaxWidth(0.86f))
        PlateButton(text = stringResource(R.string.btn_restart), onClick = onRestart, modifier = Modifier.fillMaxWidth(0.86f))
        PlateButton(text = stringResource(R.string.btn_menu), onClick = onMenu, modifier = Modifier.fillMaxWidth(0.86f))
    }
}
