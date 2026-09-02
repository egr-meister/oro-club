package com.oroclub.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oroclub.game.data.GameRepository
import com.oroclub.game.integration.AppsFlyerManager
import com.oroclub.game.integration.DeepLinkRouter
import com.oroclub.game.integration.IntegrationStorage
import com.oroclub.game.integration.RouteDecision
import com.oroclub.game.integration.TrafficRouter
import com.oroclub.game.ui.components.LoadingSweep
import com.oroclub.game.ui.components.OroBackground
import com.oroclub.game.ui.nav.OroNavGraph
import com.oroclub.game.ui.screens.OfferWebViewScreen
import com.oroclub.game.ui.theme.TextMuted
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val ATTRIBUTION_TIMEOUT_MS = 8_000L

/**
 * Startup orchestrator. Decides, once per install, whether this device sees the game
 * (White) or the offer (Black), and hands push deep links priority over both.
 */
@Composable
fun AetherRoot(repository: GameRepository) {
    var deepLinkUrl by remember { mutableStateOf<String?>(null) }
    var routeGate by remember { mutableStateOf<RouteDecision?>(null) }

    DisposableEffect(Unit) {
        DeepLinkRouter.setHandler { url -> deepLinkUrl = url }
        onDispose { DeepLinkRouter.clearHandler() }
    }

    LaunchedEffect(Unit) {
        // On a first launch the offer URL is worthless until conversion data lands, so
        // wait - but bounded, because AppsFlyer may never answer and a splash that never
        // ends is worse than an unattributed offer.
        if (!IntegrationStorage.hasRouteDecision) {
            withTimeoutOrNull(ATTRIBUTION_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    AppsFlyerManager.setOnAttributionResolved {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    continuation.invokeOnCancellation {
                        AppsFlyerManager.setOnAttributionResolved(null)
                    }
                }
            }
            AppsFlyerManager.setOnAttributionResolved(null)
        }
        // The router reads the whole stored attribution map, not a hand-picked subset.
        TrafficRouter.route { decision -> routeGate = decision }
    }

    val currentDeepLink = deepLinkUrl
    val decision = routeGate
    when {
        // A deep link is an explicit instruction and outranks the routing decision.
        currentDeepLink != null -> OfferWebViewScreen(currentDeepLink)
        decision is RouteDecision.Black -> OfferWebViewScreen(decision.url)
        decision is RouteDecision.White -> OroNavGraph(repository = repository)
        else -> SplashContent()
    }
}

/**
 * The club wordmark on the game's own background, plus a moving loading bar.
 *
 * This screen can be held for several seconds while attribution resolves and the routing
 * probe runs. A still image reads as a frozen app, so something has to move.
 */
@Composable
private fun SplashContent() {
    OroBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Assets.logo),
                contentDescription = "Oro Club",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(Assets.LOGO_ASPECT)
            )
            LoadingSweep(
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .offset(y = (-16).dp)
            )
            Text(
                text = "LOADING",
                color = TextMuted,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .offset(y = (-16).dp)
                    // The bar already says "working"; the word is for screen readers and
                    // for anyone who reads text before they notice motion.
                    .semantics { contentDescription = "Loading Oro Club" }
            )
        }
    }
}
