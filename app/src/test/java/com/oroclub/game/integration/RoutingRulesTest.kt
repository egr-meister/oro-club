package com.oroclub.game.integration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingRulesTest {

    private val requested = "https://d.cfd/android1?campaign=android1"

    @Test
    fun `404 means White`() {
        val decision = RoutingRules.decide(ProbeResult(404, requested), requested)
        assertEquals(RouteDecision.White, decision)
    }

    @Test
    fun `every other status means Black`() {
        listOf(200, 201, 204, 301, 302, 400, 401, 403, 418, 500, 502, 503).forEach { code ->
            val decision = RoutingRules.decide(ProbeResult(code, requested), requested)
            assertTrue("code $code should be Black", decision is RouteDecision.Black)
        }
    }

    @Test
    fun `Black carries the final URL after redirects`() {
        val decision = RoutingRules.decide(
            ProbeResult(200, "https://d.cfd/final?x=1"),
            requested
        )
        assertEquals(RouteDecision.Black("https://d.cfd/final?x=1"), decision)
    }

    @Test
    fun `a missing or blank final URL falls back to the requested one`() {
        assertEquals(
            RouteDecision.Black(requested),
            RoutingRules.decide(ProbeResult(200, null), requested)
        )
        assertEquals(
            RouteDecision.Black(requested),
            RoutingRules.decide(ProbeResult(200, "   "), requested)
        )
    }

    @Test
    fun `the probe seam can be faked without a network`() {
        val probe = OfferProbe { url -> ProbeResult(404, url) }
        assertEquals(RouteDecision.White, RoutingRules.decide(probe.probe(requested), requested))
    }
}
