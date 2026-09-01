package com.oroclub.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oroclub.game.domain.GameLogic
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.PlateButton
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.components.RoundPlate
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.vm.ProgressUiState

/**
 * Nine level bubbles. A locked level is dimmed AND carries a padlock glyph plus
 * a "Locked" content description, so the state never depends on colour alone.
 */
@Composable
fun LevelsScreen(
    state: ProgressUiState,
    onLevel: (Int) -> Unit,
    onRules: () -> Unit,
    onBack: () -> Unit
) {
    OroScreen(title = "Matching Pairs", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyText(
                text = "Cleared ${state.completedLevels} of ${state.totalLevels} levels",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)
            ) {
                items(GameLogic.levels, key = { it.level }) { config ->
                    val unlocked = state.isLevelUnlocked(config.level)
                    val completed = state.isLevelCompleted(config.level)
                    LevelBubble(
                        level = config.level,
                        pairs = config.pairs,
                        unlocked = unlocked,
                        completed = completed,
                        best = state.bestPairs(config.level),
                        onClick = { if (unlocked) onLevel(config.level) }
                    )
                }
            }
            PlateButton(
                text = "GAME RULES",
                onClick = onRules,
                modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun LevelBubble(
    level: Int,
    pairs: Int,
    unlocked: Boolean,
    completed: Boolean,
    best: Int,
    onClick: () -> Unit
) {
    val description = when {
        !unlocked -> "Level $level, locked. Clear level ${level - 1} to unlock."
        completed -> "Level $level, cleared, $pairs pairs."
        else -> "Level $level, unlocked, $pairs pairs, best $best."
    }
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        RoundPlate(
            modifier = Modifier.fillMaxSize(),
            dimmed = !unlocked,
            onClick = if (unlocked) onClick else null,
            contentDescription = description
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (unlocked) level.toString() else "🔒",
                    color = if (unlocked) Gold else TextMuted,
                    fontSize = if (unlocked) 26.sp else 20.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (unlocked) (if (completed) "CLEARED" else "$pairs pairs") else "LOCKED",
                    color = TextMuted,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
