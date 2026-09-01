package com.oroclub.game.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.components.PauseOverlay
import com.oroclub.game.ui.components.PauseWhenBackgrounded
import com.oroclub.game.ui.components.TimerBar
import com.oroclub.game.ui.theme.CorrectGreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gem
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.WrongRed
import com.oroclub.game.ui.vm.CardUi
import com.oroclub.game.ui.vm.PairsUiState

/**
 * The Matching Pairs board: a countdown, a pair counter and a grid of cards.
 * Card backs use the round club token art; faces are drawn panels with the item
 * art on top.
 */
@Composable
fun PairsScreen(
    state: PairsUiState,
    onFlip: (Int) -> Unit,
    onPause: () -> Unit,
    onLeaveForeground: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onBack: () -> Unit
) {
    PauseWhenBackgrounded(onLeaveForeground = onLeaveForeground)

    Box(modifier = Modifier.fillMaxSize()) {
        OroScreen(title = "Level ${state.level}", onBack = onBack, onPause = onPause) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pairs ${state.matchedPairs}/${state.totalPairs}",
                        color = Gold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    TimerBar(progress = state.timeProgress, modifier = Modifier.weight(1f))
                    Text(
                        text = formatSeconds(state.secondsLeft),
                        color = if (state.secondsLeft <= 10) WrongRed else Gem,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(state.columns.coerceIn(2, 5)),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp)
                ) {
                    items(state.cards, key = { it.id }) { card ->
                        PairCard(card = card, onClick = { onFlip(card.id) })
                    }
                }
            }
        }

        PauseOverlay(
            visible = state.paused,
            onContinue = onResume,
            onRestart = onRestart,
            onMenu = onMenu
        )
    }
}

@Composable
private fun PairCard(card: CardUi, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val flip by animateFloatAsState(
        targetValue = if (card.faceUp || card.matched) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "cardFlip"
    )
    val showFace = flip > 0.5f
    val description = when {
        card.matched -> "${Assets.itemName(card.itemIndex)}, matched"
        card.faceUp -> Assets.itemName(card.itemIndex)
        else -> "Face-down card, tap to flip"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                // A subtle vertical squash reads as a flip without a 3D camera.
                scaleX = 1f - 0.12f * kotlin.math.sin(flip * Math.PI).toFloat()
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Gold),
                enabled = !card.matched && !card.faceUp,
                role = Role.Button,
                onClick = onClick
            )
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (showFace) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(DeepRoyal, Panel)))
                    .border(
                        width = 2.dp,
                        color = if (card.matched) CorrectGreen else GoldDark,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Assets.item(card.itemIndex)),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().alpha(if (card.matched) 0.92f else 1f)
                )
            }
        } else {
            Image(
                painter = painterResource(Assets.roundPlate),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun formatSeconds(total: Int): String {
    val safe = total.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return if (seconds < 10) "$minutes:0$seconds" else "$minutes:$seconds"
}
