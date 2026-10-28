package com.oroclub.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.GoldDivider
import com.oroclub.game.ui.components.GoldTitle
import com.oroclub.game.ui.components.OroProgressBar
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary
import com.oroclub.game.ui.vm.ProgressUiState

/** Best score per quiz deck plus how many Pairs levels are cleared. */
@Composable
fun ResultsSummaryScreen(
    state: ProgressUiState,
    onBack: () -> Unit
) {
    OroScreen(title = stringResource(R.string.results_title), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GoldTitle(
                text = stringResource(R.string.quiz_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.quizProgress.isEmpty()) {
                BodyText(text = stringResource(R.string.results_no_decks), modifier = Modifier.fillMaxWidth())
            } else {
                state.quizProgress.forEach { category ->
                    SummaryRow(
                        badgeItemIndex = category.badgeItemIndex,
                        title = category.title,
                        value = "${category.best}/${category.total}",
                        progress = if (category.total == 0) 0f else category.best.toFloat() / category.total,
                        description = "${category.title}: best ${category.best} of ${category.total}"
                    )
                }
            }

            GoldDivider(modifier = Modifier.align(Alignment.CenterHorizontally))

            GoldTitle(
                text = stringResource(R.string.levels_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            SummaryRow(
                badgeItemIndex = 11,
                title = stringResource(R.string.results_levels_cleared),
                value = "${state.completedLevels}/${state.totalLevels}",
                progress = if (state.totalLevels == 0) 0f
                else state.completedLevels.toFloat() / state.totalLevels,
                description = "Levels cleared: ${state.completedLevels} of ${state.totalLevels}"
            )

            BodyText(
                text = stringResource(R.string.results_progress_note),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    badgeItemIndex: Int,
    title: String,
    value: String,
    progress: Float,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(DeepRoyal, Panel)))
            .border(1.dp, GoldDark, RoundedCornerShape(20.dp))
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(Assets.item(badgeItemIndex)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(46.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            OroProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
        }
        Text(
            text = value,
            color = if (progress >= 1f) Gold else TextMuted,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
