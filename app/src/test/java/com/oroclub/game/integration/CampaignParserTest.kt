package com.oroclub.game.integration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignParserTest {

    @Test
    fun `the reference campaign explodes into six positional subs`() {
        val parsed = CampaignParser.parse("android1_AR_sub3%26_sub4_WBZ_sub6")

        assertEquals(6, parsed.size)
        assertEquals("android1", parsed["sub1"])
        assertEquals("AR", parsed["sub2"])
        assertEquals("sub3%26", parsed["sub3"])
        assertEquals("sub4", parsed["sub4"])
        assertEquals("WBZ", parsed["sub5"])
        assertEquals("sub6", parsed["sub6"])
    }

    @Test
    fun `order is preserved`() {
        val parsed = CampaignParser.parse("a_b_c_d")
        assertEquals(listOf("sub1", "sub2", "sub3", "sub4"), parsed.keys.toList())
    }

    @Test
    fun `an empty campaign yields no subs at all`() {
        assertTrue(CampaignParser.parse("").isEmpty())
    }

    @Test
    fun `empty segments keep the position of the fields after them`() {
        val parsed = CampaignParser.parse("android1__AR")
        assertEquals(3, parsed.size)
        assertEquals("android1", parsed["sub1"])
        assertEquals("", parsed["sub2"])
        assertEquals("AR", parsed["sub3"])
    }

    @Test
    fun `a campaign without separators is a single sub1`() {
        val parsed = CampaignParser.parse("android1")
        assertEquals(1, parsed.size)
        assertEquals("android1", parsed["sub1"])
    }

    @Test
    fun `segment values are never decoded`() {
        val parsed = CampaignParser.parse("a%26b_c%20d")
        assertEquals("a%26b", parsed["sub1"])
        assertEquals("c%20d", parsed["sub2"])
    }

    @Test
    fun `toQuery leaves already encoded values alone`() {
        val query = CampaignParser.toQuery(
            linkedMapOf("sub1" to "android1", "sub3" to "sub3%26")
        )
        assertEquals("sub1=android1&sub3=sub3%26", query)
        assertTrue(!query.contains("%2526"))
    }

    @Test
    fun `toQuery encodes characters that would break the query`() {
        val query = CampaignParser.toQuery(linkedMapOf("k" to "a&b=c d"))
        assertEquals("k=a%26b%3Dc%20d", query)
    }

    @Test
    fun `encoding is idempotent`() {
        val once = CampaignParser.toQuery(linkedMapOf("k" to "a&b"))
        val twice = CampaignParser.toQuery(linkedMapOf("k" to once.substringAfter('=')))
        assertEquals(once, twice)
    }
}
