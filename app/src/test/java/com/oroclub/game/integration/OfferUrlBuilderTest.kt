package com.oroclub.game.integration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfferUrlBuilderTest {

    private val referenceAttribution = linkedMapOf(
        "network" to "unityads",
        "tracker_name" to "Oro Club",
        "campaign" to "android1_AR_sub3%26_sub4_WBZ_sub6"
    )

    // --- prily mode (sub1 in the path) ---------------------------------------

    @Test
    fun `prily reference URL carries every part`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://secret-joker.cfd/",
            attribution = referenceAttribution,
            adid = "test-adid",
            sub1InPath = true
        )

        assertTrue("path segment missing: $url", url.startsWith("https://secret-joker.cfd/android1?"))
        assertTrue(url.contains("network=unityads"))
        assertTrue(url.contains("tracker_name=Oro"))
        assertTrue("campaign was dropped: $url", url.contains("campaign=android1_AR_sub3%26_sub4_WBZ_sub6"))
        assertTrue(url.contains("&sub1=android1"))
        assertTrue(url.contains("sub2=AR"))
        assertTrue(url.contains("sub3=sub3%26"))
        assertTrue(url.contains("sub4=sub4"))
        assertTrue(url.contains("sub5=WBZ"))
        assertTrue(url.contains("sub6=sub6"))
        assertTrue(url.contains("adid=test-adid"))
        assertFalse("double encoding: $url", url.contains("%2526"))
    }

    @Test
    fun `prily keeps sub1 in the path and in the query at once`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", referenceAttribution, null, sub1InPath = true)
        assertTrue(url.startsWith("https://d.cfd/android1?"))
        assertTrue(url.contains("&sub1=android1"))
    }

    @Test
    fun `prily with an empty sub1 adds no path segment`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/",
            attribution = linkedMapOf("campaign" to "_AR_WBZ", "network" to "fb"),
            adid = null,
            sub1InPath = true
        )
        assertTrue("unexpected path segment: $url", url.startsWith("https://d.cfd/?"))
        assertTrue(url.contains("network=fb"))
        assertTrue(url.contains("campaign=_AR_WBZ"))
        assertTrue(url.contains("sub2=AR"))
        assertTrue(url.contains("sub3=WBZ"))
    }

    @Test
    fun `prily keeps an existing base path and appends sub1`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://domain.com/offer/",
            attribution = linkedMapOf("campaign" to "test"),
            adid = null,
            sub1InPath = true
        )
        assertTrue(url.startsWith("https://domain.com/offer/test?"))
    }

    @Test
    fun `prily building twice over its own output does not duplicate the segment`() {
        val once = OfferUrlBuilder.build("https://d.cfd/", referenceAttribution, "adid-1", sub1InPath = true)
        val twice = OfferUrlBuilder.build(once, referenceAttribution, "adid-1", sub1InPath = true)
        assertTrue(twice.startsWith("https://d.cfd/android1?"))
        assertFalse(twice.contains("/android1/android1"))
    }

    // --- aso mode (path untouched) -------------------------------------------

    @Test
    fun `aso does not touch the path`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", referenceAttribution, null, sub1InPath = false)
        assertTrue(url.startsWith("https://d.cfd/?"))
        assertTrue("sub1 must still be in the query", url.contains("sub1=android1"))
        assertFalse("no path segment in aso: $url", url.startsWith("https://d.cfd/android1"))
    }

    // --- shared rules --------------------------------------------------------

    @Test
    fun `no attribution parameter is ever lost`() {
        val params = linkedMapOf(
            "network" to "restricted",
            "campaign" to "x_y",
            "tracker_token" to "abc123",
            "some_future_key" to "value",
            "blank_on_purpose" to ""
        )
        val url = OfferUrlBuilder.build("https://d.cfd/", params, null)
        val query = url.substringAfter('?')
        val emitted = query.split('&').associate {
            val eq = it.indexOf('=')
            it.substring(0, eq) to it.substring(eq + 1)
        }
        params.keys.forEach { key ->
            assertTrue("lost parameter $key in $url", emitted.containsKey(key))
        }
        assertEquals("", emitted["blank_on_purpose"])
        assertTrue(emitted.containsKey("sub1"))
        assertTrue(emitted.containsKey("sub2"))
    }

    @Test
    fun `extras win over same-named campaign segments`() {
        // A 12+ segment campaign produces its own sub12; the device signal must win.
        val longCampaign = (1..13).joinToString("_") { "seg$it" }
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/",
            attribution = linkedMapOf("campaign" to longCampaign),
            adid = null,
            extras = linkedMapOf("sub12" to "85_true")
        )
        assertTrue(url.contains("sub12=85_true"))
        assertEquals("sub12 must not appear twice", 1, url.split("sub12=").size - 1)
    }

    @Test
    fun `parsed subs override any sub the network sent itself`() {
        val params = linkedMapOf(
            "campaign" to "android1_AR",
            "sub1" to "stale",
            "sub2" to "stale"
        )
        val url = OfferUrlBuilder.build("https://d.cfd/", params, null)
        assertTrue(url.contains("sub1=android1"))
        assertTrue(url.contains("sub2=AR"))
        assertFalse(url.contains("stale"))
        assertEquals(1, url.split("sub1=").size - 1)
    }

    @Test
    fun `query parameters already on the base URL survive`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/land?utm=x",
            attribution = linkedMapOf("campaign" to "one"),
            adid = null
        )
        assertTrue(url.contains("utm=x"))
        assertTrue(url.contains("campaign=one"))
    }

    @Test
    fun `a fragment on the base URL is preserved at the end`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/#promo",
            attribution = linkedMapOf("campaign" to "one"),
            adid = null
        )
        assertTrue(url.endsWith("#promo"))
    }

    @Test
    fun `a blank adid is not emitted`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", referenceAttribution, "   ")
        assertFalse(url.contains("adid"))
    }

    @Test
    fun `a blank base URL yields an empty result rather than a broken one`() {
        assertEquals("", OfferUrlBuilder.build("", referenceAttribution, "adid-1"))
        assertEquals("", OfferUrlBuilder.build("   ", referenceAttribution, "adid-1"))
    }

    @Test
    fun `no attribution at all still produces a usable URL`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", emptyMap(), null)
        assertEquals("https://d.cfd/", url)
    }
}
