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
import androidx.compose.ui.unit.dp
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
                    text = if (won) "Well Done" else "Out of Time",
                    style = MaterialTheme.typography.headlineMedium
                )
                GoldDivider()
                StrongText(
                    text = if (won) "Level $level cleared" else "Level $level not cleared",
                    color = if (won) CorrectGreen else WrongRed,
                    style = MaterialTheme.typography.titleLarge
                )
                StrongText(
                    text = "Pairs matched: $matched/$total",
                    style = MaterialTheme.typography.headlineSmall
                )
                BodyText(
                    text = when {
                        won && hasNextLevel -> "The next table is open."
                        won -> "Every table in the club is cleared."
                        else -> "Try the same table again — the deck reshuffles."
                    }
                )
                PlateButton(
                    text = if (won && hasNextLevel) "NEXT LEVEL" else "CONTINUE",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(0.86f)
                )
                PlateButton("RESTART", onRestart, Modifier.fillMaxWidth(0.86f))
                PlateButton("MENU", onMenu, Modifier.fillMaxWidth(0.86f))
            }
        }
    }
}
