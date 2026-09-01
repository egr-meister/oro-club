package com.oroclub.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oroclub.game.ui.components.GoldTitle
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary

private data class RuleBlock(val title: String, val body: String)

private val rules = listOf(
    RuleBlock(
        "Quiz",
        "Pick one of three themed decks. Each deck holds ten questions with one " +
            "correct answer. Tap an answer and it is revealed immediately — the correct " +
            "option is marked with a check, a wrong pick with a cross — then the next " +
            "question arrives on its own. Your best score per deck is kept."
    ),
    RuleBlock(
        "Matching Pairs",
        "Nine levels, each with more cards and a longer clock. Flip two cards: a " +
            "matching pair stays face up, a mismatch turns back over. Clear every pair " +
            "before the timer runs out to win the level. Finish a level to unlock the next one."
    ),
    RuleBlock(
        "Pause",
        "Tap the pause token at any time. The clock stops while the panel is open, and " +
            "you can continue, restart the round, or step back to the menu."
    ),
    RuleBlock(
        "Your progress",
        "Best quiz scores, cleared levels and the sound switch are saved on this device " +
            "and nowhere else. Reset them any time from Settings."
    ),
    RuleBlock(
        "Fair play",
        "Oro Club is a single-player game for entertainment. There is no wagering, no " +
            "real-money play, no prizes and no online component of any kind."
    )
)

/** Static rules text, reachable from the menu and from the level select. */
@Composable
fun RulesScreen(onBack: () -> Unit) {
    OroScreen(title = "Game Rules", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            rules.forEach { block ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(DeepRoyal, Panel)))
                        .border(1.dp, GoldDark, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldTitle(
                        text = block.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = block.body,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
            }
            Text(
                text = "Oro Club — Golden Joker Club",
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
