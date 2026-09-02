package com.oroclub.game.integration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfferUrlBuilderTest {

    private val referenceParams = linkedMapOf(
        "af_status" to "Non-organic",
        "media_source" to "facebook",
        "campaign" to "android1_AR_sub3%26_sub4_WBZ_sub6"
    )

    @Test
    fun `the reference URL carries every part`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://secret-joker.cfd/",
            appsFlyerParams = referenceParams,
            appsFlyerId = "test-af-id"
        )

        assertTrue("path segment missing: $url", url.startsWith("https://secret-joker.cfd/android1?"))
        assertTrue(url.contains("af_status=Non-organic"))
        assertTrue(url.contains("media_source=facebook"))
        assertTrue("campaign was dropped: $url", url.contains("campaign=android1_AR_sub3%26_sub4_WBZ_sub6"))
        assertTrue(url.contains("&sub1=android1"))
        assertTrue(url.contains("sub2=AR"))
        assertTrue(url.contains("sub3=sub3%26"))
        assertTrue(url.contains("sub4=sub4"))
        assertTrue(url.contains("sub5=WBZ"))
        assertTrue(url.contains("sub6=sub6"))
        assertTrue(url.contains("appsflyer_id=test-af-id"))
        assertFalse("double encoding: $url", url.contains("%2526"))
    }

    @Test
    fun `no AppsFlyer parameter is ever lost`() {
        val params = linkedMapOf(
            "af_status" to "Organic",
            "media_source" to "restricted",
            "campaign" to "x_y",
            "af_siteid" to "123",
            "some_future_key" to "value",
            "install_time" to "2026-09-01 12:00:00.000",
            "is_first_launch" to "true",
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
    fun `sub1 is in the path and in the query at the same time`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", referenceParams, null)
        assertTrue(url.startsWith("https://d.cfd/android1?"))
        assertTrue(url.contains("&sub1=android1"))
    }

    @Test
    fun `a missing campaign adds no path segment`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/",
            appsFlyerParams = linkedMapOf("af_status" to "Organic"),
            appsFlyerId = "af-1"
        )
        assertTrue(url.startsWith("https://d.cfd/?"))
        assertTrue(url.contains("af_status=Organic"))
        assertTrue(url.contains("appsflyer_id=af-1"))
        assertFalse(url.contains("sub1="))
    }

    @Test
    fun `an empty sub1 adds no path segment but keeps the other parameters`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/",
            appsFlyerParams = linkedMapOf("campaign" to "_AR_WBZ", "media_source" to "fb"),
            appsFlyerId = null
        )
        assertTrue("unexpected path segment: $url", url.startsWith("https://d.cfd/?"))
        assertTrue(url.contains("media_source=fb"))
        assertTrue(url.contains("campaign=_AR_WBZ"))
        assertTrue(url.contains("sub2=AR"))
        assertTrue(url.contains("sub3=WBZ"))
    }

    @Test
    fun `an existing base path keeps its segments and gains sub1 at the end`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://domain.com/offer/",
            appsFlyerParams = linkedMapOf("campaign" to "test"),
            appsFlyerId = null
        )
        assertTrue(url.startsWith("https://domain.com/offer/test?"))
    }

    @Test
    fun `a base path without a trailing slash also gains sub1`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://domain.com/offer",
            appsFlyerParams = linkedMapOf("campaign" to "test"),
            appsFlyerId = null
        )
        assertTrue(url.startsWith("https://domain.com/offer/test?"))
    }

    @Test
    fun `building twice over its own output does not duplicate the path segment`() {
        val once = OfferUrlBuilder.build("https://d.cfd/", referenceParams, "af-1")
        val twice = OfferUrlBuilder.build(once, referenceParams, "af-1")
        assertTrue(twice.startsWith("https://d.cfd/android1?"))
        assertFalse(twice.contains("/android1/android1"))
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
        // Re-putting an existing key replaces it in place, so no key is emitted twice.
        assertEquals(1, url.split("sub1=").size - 1)
    }

    @Test
    fun `query parameters already on the base URL survive`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/land?utm=x",
            appsFlyerParams = linkedMapOf("campaign" to "one"),
            appsFlyerId = null
        )
        assertTrue(url.startsWith("https://d.cfd/land/one?"))
        assertTrue(url.contains("utm=x"))
    }

    @Test
    fun `a fragment on the base URL is preserved at the end`() {
        val url = OfferUrlBuilder.build(
            baseUrl = "https://d.cfd/#promo",
            appsFlyerParams = linkedMapOf("campaign" to "one"),
            appsFlyerId = null
        )
        assertTrue(url.startsWith("https://d.cfd/one?"))
        assertTrue(url.endsWith("#promo"))
    }

    @Test
    fun `a blank appsflyer id is not emitted`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", referenceParams, "   ")
        assertFalse(url.contains("appsflyer_id"))
    }

    @Test
    fun `a blank base URL yields an empty result rather than a broken one`() {
        assertEquals("", OfferUrlBuilder.build("", referenceParams, "af-1"))
        assertEquals("", OfferUrlBuilder.build("   ", referenceParams, "af-1"))
    }

    @Test
    fun `no attribution at all still produces a usable URL`() {
        val url = OfferUrlBuilder.build("https://d.cfd/", emptyMap(), null)
        assertEquals("https://d.cfd/", url)
    }
}
