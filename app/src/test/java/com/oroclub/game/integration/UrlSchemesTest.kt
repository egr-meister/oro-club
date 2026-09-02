package com.oroclub.game.integration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSchemesTest {

    @Test
    fun `every allowed scheme stays inside the WebView`() {
        listOf(
            "http://d.cfd/a",
            "https://d.cfd/a",
            "about:blank",
            "srcdoc:something",
            "blob:https://d.cfd/uuid",
            "data:text/html,<h1>hi</h1>",
            "javascript:void(0)",
            "file:///android_asset/x.html"
        ).forEach { url ->
            assertFalse("$url should stay internal", UrlSchemes.isExternal(url))
        }
    }

    @Test
    fun `app schemes leave the WebView`() {
        listOf(
            "intent://scan/#Intent;scheme=zxing;end",
            "market://details?id=com.oroclub.game",
            "tg://resolve?domain=x",
            "whatsapp://send?text=hi",
            "tel:+380000000000",
            "mailto:a@b.c",
            "bnl://pay"
        ).forEach { url ->
            assertTrue("$url should be external", UrlSchemes.isExternal(url))
        }
    }

    @Test
    fun `relative URLs have no scheme and stay internal`() {
        listOf("/path/to/page", "page.html", "../up", "?query=1", "#anchor").forEach { url ->
            assertNull(UrlSchemes.schemeOf(url))
            assertFalse(UrlSchemes.isExternal(url))
        }
    }

    @Test
    fun `a colon inside a path is not a scheme`() {
        assertNull(UrlSchemes.schemeOf("/path:with:colons"))
        assertFalse(UrlSchemes.isExternal("/path:with:colons"))
    }

    @Test
    fun `a scheme may not start with a digit`() {
        assertNull(UrlSchemes.schemeOf("2go:something"))
        assertFalse(UrlSchemes.isExternal("2go:something"))
    }

    @Test
    fun `scheme detection is case insensitive`() {
        assertEquals("https", UrlSchemes.schemeOf("HTTPS://d.cfd"))
        assertFalse(UrlSchemes.isExternal("HTTPS://d.cfd"))
        assertTrue(UrlSchemes.isExternal("MARKET://details?id=x"))
    }

    @Test
    fun `schemes may contain plus dot and dash`() {
        assertEquals("coap+ws", UrlSchemes.schemeOf("coap+ws://host"))
        assertEquals("x-app.v2", UrlSchemes.schemeOf("x-app.v2://host"))
        assertTrue(UrlSchemes.isExternal("x-app.v2://host"))
    }

    @Test
    fun `leading whitespace does not hide a scheme`() {
        assertEquals("market", UrlSchemes.schemeOf("  market://details"))
        assertTrue(UrlSchemes.isExternal("  market://details"))
    }

    @Test
    fun `an empty URL is internal`() {
        assertNull(UrlSchemes.schemeOf(""))
        assertFalse(UrlSchemes.isExternal(""))
    }
}
