package com.oroclub.game.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.OroProgressBar
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.components.PauseOverlay
import com.oroclub.game.ui.theme.CorrectGreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary
import com.oroclub.game.ui.theme.WrongRed
import com.oroclub.game.ui.vm.QuizUiState

/**
 * One question at a time with a progress bar and an "n/10" counter.
 * Answer state is signalled by BOTH colour and a glyph (check / cross), so
 * colour is never the only cue.
 */
@Composable
fun QuizScreen(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OroScreen(title = state.categoryTitle, onBack = onBack, onPause = onPause) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 6.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OroProgressBar(
                        progress = state.progress,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${state.displayPosition}/${state.questionCount}",
                        color = Gold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 116.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.verticalGradient(listOf(DeepRoyal, Panel)))
                        .border(1.5.dp, GoldDark, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.questionText,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }

                state.options.forEachIndexed { index, option ->
                    AnswerRow(
                        text = option,
                        index = index,
                        state = state,
                        onSelect = onSelect
                    )
                }

                BodyText(
                    text = "Score so far: ${state.score}",
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth()
                )
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
private fun AnswerRow(
    text: String,
    index: Int,
    state: QuizUiState,
    onSelect: (Int) -> Unit
) {
    val revealed = state.locked && state.selectedIndex != null
    val isCorrectOption = index == state.correctIndex
    val isPicked = state.selectedIndex == index

    val targetBorder = when {
        revealed && isCorrectOption -> CorrectGreen
        revealed && isPicked -> WrongRed
        else -> GoldDark
    }
    val borderColor by animateColorAsState(targetValue = targetBorder, label = "answerBorder")

    val fill: Brush = when {
        revealed && isCorrectOption -> Brush.horizontalGradient(listOf(Color(0xFF14442E), CorrectGreen))
        revealed && isPicked -> Brush.horizontalGradient(listOf(Color(0xFF4A1414), WrongRed))
        else -> Brush.horizontalGradient(listOf(DeepRoyal, Panel))
    }

    // A glyph, not just a colour, carries the result.
    val marker = when {
        revealed && isCorrectOption -> "✓"
        revealed && isPicked -> "✗"
        else -> ('A' + index).toString()
    }

    val stateWord = when {
        revealed && isCorrectOption -> "correct answer"
        revealed && isPicked -> "wrong answer"
        else -> "answer option ${index + 1}"
    }

    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fill)
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Gold),
                enabled = !state.locked && !state.paused && !state.finished,
                role = Role.Button
            ) { onSelect(index) }
            .semantics { contentDescription = "$text, $stateWord" }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(Gold, GoldDark))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = marker,
                color = DeepRoyal,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}
