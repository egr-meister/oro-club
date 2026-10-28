package com.oroclub.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oroclub.game.R
import com.oroclub.game.data.GameRepository
import com.oroclub.game.integration.AdjustManager
import com.oroclub.game.integration.DeepLinkRouter
import com.oroclub.game.integration.IntegrationStorage
import com.oroclub.game.integration.RouteDecision
import com.oroclub.game.integration.TrafficRouter
import com.oroclub.game.ui.components.LoadingSweep
import com.oroclub.game.ui.components.OroBackground
import com.oroclub.game.ui.nav.OroNavGraph
import com.oroclub.game.ui.screens.OfferWebViewScreen
import com.oroclub.game.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val ATTRIBUTION_TIMEOUT_MS = 8_000L

/**
 * Startup orchestrator. Decides, once per install, whether this device sees the game
 * (White) or the offer (Black). The WebView opens the campaign link itself and its client
 * turns the tracker's answer into the verdict; this composable only reacts to it.
 *
 * A push deep link outranks the routing decision and pins/changes nothing.
 */
@Composable
fun AetherRoot(repository: GameRepository) {
    val scope = rememberCoroutineScope()

    var deepLinkUrl by remember { mutableStateOf<String?>(null) }
    var deepLinkFinal by remember { mutableStateOf<String?>(null) }

    var gate by remember { mutableStateOf<RouteDecision?>(null) }
    var painted by remember { mutableStateOf(false) }
    var reroutes by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        DeepLinkRouter.setHandler { url -> deepLinkUrl = url }
        onDispose { DeepLinkRouter.clearHandler() }
    }

    // Route, and re-route when a cached address turns out dead. Attribution is awaited only
    // when nothing is pinned yet (the first install): the link is worthless without source
    // data, but a splash that never ends is worse, so the wait is bounded.
    LaunchedEffect(reroutes) {
        if (!withContext(Dispatchers.IO) { IntegrationStorage.hasRouteDecision() }) {
            withTimeoutOrNull(ATTRIBUTION_TIMEOUT_MS + 2_000L) {
                suspendCancellableCoroutine { continuation ->
                    AdjustManager.awaitAttribution(ATTRIBUTION_TIMEOUT_MS) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            }
        }
        TrafficRouter.route { decision -> gate = decision }
    }

    // A pushed link is opened as sent, with only push_token/push_id appended (off-thread:
    // appending waits on the token latch).
    LaunchedEffect(deepLinkUrl) {
        val url = deepLinkUrl
        deepLinkFinal = if (url.isNullOrBlank()) null
        else withContext(Dispatchers.IO) { TrafficRouter.appendPushParams(url) }
    }

    val currentDeepLink = deepLinkFinal
    val decision = gate
    when {
        // A deep link is an explicit instruction: show it, change no verdict, pin nothing.
        currentDeepLink != null -> OfferWebViewScreen(
            url = currentDeepLink,
            fromCache = false,
            onWhiteVerdict = {},
            onOfferCommitted = {},
            onReroute = {},
            onTransientWhite = {}
        )

        decision is RouteDecision.Black -> Box(Modifier.fillMaxSize()) {
            // Keyed by url AND source: a rebuilt campaign link can match a dead cached one
            // byte for byte, yet it is a different load with different error meaning.
            key(decision.url, decision.fromCache) {
                OfferWebViewScreen(
                    url = decision.url,
                    fromCache = decision.fromCache,
                    onWhiteVerdict = {
                        IntegrationStorage.lockWhite()
                        gate = RouteDecision.White
                    },
                    onOfferCommitted = { url ->
                        scope.launch(Dispatchers.IO) { IntegrationStorage.saveOfferUrl(url) }
                        painted = true
                    },
                    onReroute = {
                        scope.launch(Dispatchers.IO) { IntegrationStorage.clearOfferUrl() }
                        painted = false
                        reroutes++
                    },
                    onTransientWhite = {
                        // White for this launch only: nothing is locked, nothing pinned.
                        gate = RouteDecision.White
                    }
                )
            }
            // The same splash stays over the offer until a page renders, so a White install
            // never sees the offer flash past on the way to its 404.
            if (!painted) SplashContent()
        }

        decision is RouteDecision.White -> OroNavGraph(repository = repository)
        else -> SplashContent()
    }
}

/**
 * The club wordmark on the game's own background, plus a moving loading bar.
 *
 * This screen can be held for several seconds while attribution resolves and the offer
 * loads. A still image reads as a frozen app, so something has to move.
 */
@Composable
private fun SplashContent() {
    val loadingLabel = stringResource(R.string.loading)
    OroBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Assets.logo),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(0.7f).aspectRatio(Assets.LOGO_ASPECT)
            )
            LoadingSweep(
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .offset(y = (-16).dp)
            )
            Text(
                text = loadingLabel,
                color = TextMuted,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .offset(y = (-16).dp)
                    // The bar already says "working"; the word is for screen readers.
                    .semantics { contentDescription = loadingLabel }
            )
        }
    }
}
