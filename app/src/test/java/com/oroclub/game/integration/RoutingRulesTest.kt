package com.oroclub.game.integration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingRulesTest {

    @Test
    fun `404 on the main frame means White`() {
        assertTrue(RoutingRules.isWhite(404))
    }

    @Test
    fun `every other status is not White`() {
        // 403 in particular is NOT White: it is a WAF, not the tracker's answer, and the
        // verdict is permanent.
        listOf(200, 201, 204, 301, 302, 400, 401, 403, 410, 418, 500, 502, 503).forEach { code ->
            assertFalse("code $code must not be White", RoutingRules.isWhite(code))
        }
    }
}
