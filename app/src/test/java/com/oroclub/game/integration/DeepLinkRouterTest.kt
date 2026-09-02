package com.oroclub.game.integration

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeepLinkRouterTest {

    @Before
    fun drain() {
        // DeepLinkRouter is a singleton, so each test starts from a known state: attach a
        // sink to flush anything a previous test queued, then detach it.
        DeepLinkRouter.setHandler { }
        DeepLinkRouter.clearHandler()
    }

    @After
    fun detach() {
        DeepLinkRouter.clearHandler()
    }

    @Test
    fun `a link delivered while a handler is attached goes straight through`() {
        val received = mutableListOf<String>()
        DeepLinkRouter.setHandler { received += it }

        DeepLinkRouter.handle("https://d.cfd/promo")

        assertEquals(listOf("https://d.cfd/promo"), received)
    }

    @Test
    fun `a link arriving before any handler is queued and replayed`() {
        DeepLinkRouter.handle("https://d.cfd/first")
        DeepLinkRouter.handle("https://d.cfd/second")

        val received = mutableListOf<String>()
        DeepLinkRouter.setHandler { received += it }

        assertEquals(listOf("https://d.cfd/first", "https://d.cfd/second"), received)
    }

    @Test
    fun `a queued link is delivered exactly once`() {
        DeepLinkRouter.handle("https://d.cfd/once")

        val first = mutableListOf<String>()
        DeepLinkRouter.setHandler { first += it }
        DeepLinkRouter.clearHandler()

        val second = mutableListOf<String>()
        DeepLinkRouter.setHandler { second += it }

        assertEquals(listOf("https://d.cfd/once"), first)
        assertTrue(second.isEmpty())
    }

    @Test
    fun `null and blank links are ignored`() {
        val received = mutableListOf<String>()
        DeepLinkRouter.setHandler { received += it }

        DeepLinkRouter.handle(null)
        DeepLinkRouter.handle("")
        DeepLinkRouter.handle("   ")

        assertTrue(received.isEmpty())
    }

    @Test
    fun `after clearHandler links are queued again rather than lost`() {
        val received = mutableListOf<String>()
        DeepLinkRouter.setHandler { received += it }
        DeepLinkRouter.clearHandler()

        DeepLinkRouter.handle("https://d.cfd/later")
        assertTrue(received.isEmpty())

        DeepLinkRouter.setHandler { received += it }
        assertEquals(listOf("https://d.cfd/later"), received)
    }

    @Test
    fun `the newest handler wins`() {
        val old = mutableListOf<String>()
        val new = mutableListOf<String>()
        DeepLinkRouter.setHandler { old += it }
        DeepLinkRouter.setHandler { new += it }

        DeepLinkRouter.handle("https://d.cfd/x")

        assertTrue(old.isEmpty())
        assertEquals(listOf("https://d.cfd/x"), new)
    }
}
