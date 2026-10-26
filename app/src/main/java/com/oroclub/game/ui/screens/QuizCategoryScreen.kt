package com.oroclub.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oroclub.game.R
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.MinTouchTarget
import com.oroclub.game.ui.components.OroScreen
import com.oroclub.game.ui.theme.DeepRoyal
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.GoldDark
import com.oroclub.game.ui.theme.GoldSoft
import com.oroclub.game.ui.theme.Panel
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.theme.TextPrimary
import com.oroclub.game.ui.vm.CategoryProgress
import com.oroclub.game.ui.vm.ProgressUiState

/** Quiz category picker: three themed decks with the personal best on each. */
@Composable
fun QuizCategoryScreen(
    state: ProgressUiState,
    onCategory: (String) -> Unit,
    onBack: () -> Unit
) {
    OroScreen(title = stringResource(R.string.quiz_title), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BodyText(
                text = stringResource(R.string.quiz_intro),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.quizProgress.isEmpty()) {
                BodyText(text = stringResource(R.string.quiz_none))
            } else {
                state.quizProgress.forEach { category ->
                    CategoryRow(category = category, onClick = { onCategory(category.id) })
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoryProgress,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val description = "${category.title}. Best score ${category.best} out of ${category.total}."
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(DeepRoyal, Panel)))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(Gold, GoldDark)),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Gold),
                role = Role.Button
            ) {
                SoundManager.click()
                onClick()
            }
            .semantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(Assets.item(category.badgeItemIndex)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(58.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.quiz_best, category.best, category.total),
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(
            modifier = Modifier
                .size(MinTouchTarget)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(GoldSoft, Gold, GoldDark))),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.quiz_play),
                color = DeepRoyal,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
