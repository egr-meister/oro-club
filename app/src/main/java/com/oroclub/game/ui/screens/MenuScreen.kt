package com.oroclub.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.BodyText
import com.oroclub.game.ui.components.MenuPlateButton
import com.oroclub.game.ui.components.OroScreen

/**
 * Menu. The ornate plate is tall, so every button is width-constrained to ~0.7
 * of the screen and the column scrolls on short displays or at large font
 * scales instead of clipping.
 */
@Composable
fun MenuScreen(
    onQuiz: () -> Unit,
    onPairs: () -> Unit,
    onResults: () -> Unit,
    onSettings: () -> Unit,
    onRules: () -> Unit,
    onBack: () -> Unit
) {
    OroScreen(title = "The Club", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(Assets.mascotB),
                contentDescription = "Oro Club joker mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(132.dp)
                    .aspectRatio(Assets.MASCOT_ASPECT)
            )
            BodyText(text = "Pick your table")

            MenuPlateButton("QUIZ", onQuiz, Modifier.fillMaxWidth(0.7f))
            MenuPlateButton("MATCHING PAIRS", onPairs, Modifier.fillMaxWidth(0.7f))
            MenuPlateButton("RESULTS", onResults, Modifier.fillMaxWidth(0.7f))
            MenuPlateButton("GAME RULES", onRules, Modifier.fillMaxWidth(0.7f))
            MenuPlateButton("SETTINGS", onSettings, Modifier.fillMaxWidth(0.7f))
        }
    }
}
