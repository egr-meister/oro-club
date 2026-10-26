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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.ui.components.GoldTitle
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary

private data class RuleBlock(val title: String, val body: String)

/** Static rules text, reachable from the menu and from the level select. */
@Composable
fun RulesScreen(onBack: () -> Unit) {
    val rules = listOf(
        RuleBlock(stringResource(R.string.rules_quiz_title), stringResource(R.string.rules_quiz_body)),
        RuleBlock(stringResource(R.string.rules_pairs_title), stringResource(R.string.rules_pairs_body)),
        RuleBlock(stringResource(R.string.rules_pause_title), stringResource(R.string.rules_pause_body)),
        RuleBlock(stringResource(R.string.rules_progress_title), stringResource(R.string.rules_progress_body)),
        RuleBlock(stringResource(R.string.rules_fair_title), stringResource(R.string.rules_fair_body))
    )
    OroScreen(title = stringResource(R.string.rules_title), onBack = onBack) {
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
                text = stringResource(R.string.rules_footer),
                color = TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
