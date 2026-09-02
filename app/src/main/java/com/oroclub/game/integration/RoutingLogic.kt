package com.oroclub.game.integration

sealed interface RouteDecision {
    data object White : RouteDecision
    data class Black(val url: String) : RouteDecision
}

data class ProbeResult(val statusCode: Int, val finalUrl: String?)

/** Injection seam: the real implementation talks to the network, a fake need not. */
fun interface OfferProbe {
    fun probe(url: String): ProbeResult
}

object RoutingRules {

    private const val NOT_FOUND = 404

    /**
     * 404 is the agreed "this install gets the app itself" answer. Everything else -
     * 200, 302 chains that ended somewhere, even a 500 - means the offer exists and the
     * server is simply having a moment, so Black is correct and the WebView can retry.
     */
    fun decide(result: ProbeResult, requestedUrl: String): RouteDecision =
        if (result.statusCode == NOT_FOUND) {
            RouteDecision.White
        } else {
            RouteDecision.Black(result.finalUrl?.takeIf { it.isNotBlank() } ?: requestedUrl)
        }
}
