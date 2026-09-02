package com.oroclub.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.oroclub.game.data.GameRepository
import com.oroclub.game.integration.AppsFlyerManager
import com.oroclub.game.integration.DeepLinkRouter
import com.oroclub.game.integration.IntegrationStorage
import com.oroclub.game.integration.RouteDecision
import com.oroclub.game.integration.TrafficRouter
import com.oroclub.game.ui.components.OroBackground
import com.oroclub.game.ui.nav.OroNavGraph
import com.oroclub.game.ui.screens.OfferWebViewScreen
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

/** The club wordmark on the game's own background - no new loading style is introduced. */
@Composable
private fun SplashContent() {
    OroBackground {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(Assets.logo),
                contentDescription = "Oro Club",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(Assets.LOGO_ASPECT)
            )
        }
    }
}
