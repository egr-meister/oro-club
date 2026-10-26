package com.oroclub.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.CenteredScrollContent
import com.oroclub.game.ui.components.GoldDivider
import com.oroclub.game.ui.components.GoldTitle
import com.oroclub.game.ui.components.OroBackground
import com.oroclub.game.ui.components.OroPanel
import com.oroclub.game.ui.components.PlateButton
import com.oroclub.game.ui.components.StrongText
import com.oroclub.game.ui.theme.CorrectGreen
import com.oroclub.game.ui.theme.WrongRed

/**
 * Matching Pairs results. "Well Done" on a clear, "Out of Time" on a timeout —
 * the wording, not just the colour, carries the outcome.
 */
@Composable
fun PairsResultsScreen(
    level: Int,
    matched: Int,
    total: Int,
    won: Boolean,
    hasNextLevel: Boolean,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    OroBackground {
        CenteredScrollContent {
            OroPanel(modifier = Modifier.fillMaxWidth(0.9f)) {
                Image(
                    painter = painterResource(if (won) Assets.mascotA else Assets.mascotB),
                    contentDescription = "Oro Club joker mascot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(112.dp).aspectRatio(Assets.MASCOT_ASPECT)
                )
                GoldTitle(
                    text = if (won) stringResource(R.string.pairs_well_done) else stringResource(R.string.pairs_out_of_time),
                    style = MaterialTheme.typography.headlineMedium
                )
                GoldDivider()
                StrongText(
                    text = if (won) stringResource(R.string.pairs_level_cleared, level)
                    else stringResource(R.string.pairs_level_not_cleared, level),
                    color = if (won) CorrectGreen else WrongRed,
                    style = MaterialTheme.typography.titleLarge
                )
                StrongText(
                    text = stringResource(R.string.pairs_matched, matched, total),
                    style = MaterialTheme.typography.headlineSmall
                )
                BodyText(
                    text = when {
                        won && hasNextLevel -> stringResource(R.string.pairs_next_open)
                        won -> stringResource(R.string.pairs_all_cleared)
                        else -> stringResource(R.string.pairs_try_again)
                    }
                )
                PlateButton(
                    text = if (won && hasNextLevel) stringResource(R.string.btn_next_level) else stringResource(R.string.btn_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(0.86f)
                )
                PlateButton(stringResource(R.string.btn_restart), onRestart, Modifier.fillMaxWidth(0.86f))
                PlateButton(stringResource(R.string.btn_menu), onMenu, Modifier.fillMaxWidth(0.86f))
            }
        }
    }
}
