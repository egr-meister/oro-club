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
import com.oroclub.game.ui.theme.Gold

/**
 * Quiz results. The panel is drawn in Compose so it grows with its content
 * instead of stretching a decorative frame image, and the page scrolls rather
 * than clipping at large font scales.
 */
@Composable
fun QuizResultsScreen(
    categoryTitle: String,
    score: Int,
    total: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    val verdict = when {
        total <= 0 -> "Round over"
        score == total -> "A flawless hand"
        score >= (total * 7) / 10 -> "The joker approves"
        score >= total / 2 -> "A fair run"
        else -> "The deck was unkind"
    }

    OroBackground {
        CenteredScrollContent {
            OroPanel(modifier = Modifier.fillMaxWidth(0.9f)) {
                Image(
                    painter = painterResource(Assets.mascotA),
                    contentDescription = "Oro Club joker mascot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(112.dp).aspectRatio(Assets.MASCOT_ASPECT)
                )
                GoldTitle(text = verdict, style = MaterialTheme.typography.headlineSmall)
                GoldDivider()
                StrongText(
                    text = "Score: $score/$total",
                    style = MaterialTheme.typography.displaySmall,
                    color = Gold
                )
                BodyText(text = categoryTitle)
                PlateButton("CONTINUE", onContinue, Modifier.fillMaxWidth(0.86f))
                PlateButton("RESTART", onRestart, Modifier.fillMaxWidth(0.86f))
                PlateButton("MENU", onMenu, Modifier.fillMaxWidth(0.86f))
            }
        }
    }
}
