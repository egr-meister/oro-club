package com.oroclub.game.integration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfferCacheTest {

    private val ttl = OfferCache.OFFER_TTL_MS

    @Test
    fun `a record with no timestamp is expired`() {
        assertFalse(OfferCache.isFresh(savedAt = 0L, now = 1_000_000L, ttlMs = ttl))
    }

    @Test
    fun `a clock that moved back does not grant an eternal cache`() {
        assertFalse(OfferCache.isFresh(savedAt = 2_000L, now = 1_000L, ttlMs = ttl))
    }

    @Test
    fun `fresh inside the window`() {
        val now = 10_000_000L
        assertTrue(OfferCache.isFresh(savedAt = now - (ttl / 2), now = now, ttlMs = ttl))
    }

    @Test
    fun `the window boundary is exclusive`() {
        val now = 10_000_000L
        assertFalse(OfferCache.isFresh(savedAt = now - ttl, now = now, ttlMs = ttl))
    }
}
