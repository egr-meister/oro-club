package com.oroclub.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oroclub.game.ui.Assets
import com.oroclub.game.ui.components.MenuPlateButton
import com.oroclub.game.ui.components.OroBackground
import com.oroclub.game.ui.components.RoundPlate
import com.oroclub.game.ui.theme.Gold
import com.oroclub.game.ui.theme.TextMuted
import com.oroclub.game.ui.vm.ProgressUiState

/**
 * The Oro Club front door.
 *
 * Deliberately NOT the generic mascot → title → stats-card → button-stack
 * dashboard: it is composed like a club poster. The wordmark sits high in the
 * night sky, the joker stands full-height with his feet cropped by the bottom
 * edge, and the single ornate ENTER plate overlaps his lower body so the layers
 * read as depth rather than as a list. Progress appears as one small gold club
 * token in the corner instead of a statistics card.
 */
@Composable
fun MainScreen(
    state: ProgressUiState,
    onStart: () -> Unit
) {
    OroBackground {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val tallEnough = maxHeight > 560.dp
            val mascotHeight = if (tallEnough) 0.50f else 0.42f

            // Layer 1 — the joker, cropped by the bottom edge.
            Image(
                painter = painterResource(Assets.mascotA),
                contentDescription = "Oro Club joker mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 26.dp)
                    .fillMaxHeight(mascotHeight)
                    .aspectRatio(Assets.MASCOT_ASPECT)
            )

            // Layer 2 — wordmark and tagline.
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (tallEnough) 12.dp else 0.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Assets.logo),
                    contentDescription = "Oro Club",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(if (tallEnough) 0.78f else 0.62f)
                        .aspectRatio(Assets.LOGO_ASPECT)
                )
                Text(
                    text = "TAKE A SEAT AT THE GOLDEN TABLE",
                    color = TextMuted,
                    fontSize = 12.sp,
                    letterSpacing = 2.4.sp,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth(0.86f).offset(y = (-18).dp)
                )
            }

            // Layer 3 — the club token: progress without a stats card.
            RoundPlate(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 14.dp, top = 10.dp)
                    .size(62.dp),
                contentDescription = "Levels cleared: ${state.completedLevels} of ${state.totalLevels}"
            ) {
                Text(
                    text = "${state.completedLevels}/${state.totalLevels}",
                    color = Gold,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Layer 4 — the single call to action, overlapping the mascot.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MenuPlateButton(
                    text = "ENTER THE CLUB",
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}
