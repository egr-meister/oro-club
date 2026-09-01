package com.oroclub.game

import com.oroclub.game.domain.GameLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {

    // ---------------------------------------------------------------- levels

    @Test
    fun `there are exactly nine levels with the specified configuration`() {
        assertEquals(9, GameLogic.levels.size)
        assertEquals(9, GameLogic.LEVEL_COUNT)

        val expected = listOf(
            Triple(2, 2, 60),
            Triple(3, 2, 70),
            Triple(4, 2, 70),
            Triple(6, 3, 80),
            Triple(6, 3, 80),
            Triple(8, 4, 100),
            Triple(8, 4, 100),
            Triple(10, 4, 110),
            Triple(12, 4, 120)
        )
        GameLogic.levels.forEachIndexed { index, config ->
            assertEquals(index + 1, config.level)
            assertEquals(expected[index].first, config.pairs)
            assertEquals(expected[index].second, config.columns)
            assertEquals(expected[index].third, config.seconds)
        }
    }

    @Test
    fun `levelConfig returns null for out of range levels`() {
        assertNull(GameLogic.levelConfig(0))
        assertNull(GameLogic.levelConfig(-4))
        assertNull(GameLogic.levelConfig(10))
        assertNotNull(GameLogic.levelConfig(1))
        assertNotNull(GameLogic.levelConfig(9))
    }

    @Test
    fun `no level needs more pairs than there are item icons`() {
        GameLogic.levels.forEach { config ->
            assertTrue(
                "level ${config.level} needs ${config.pairs} distinct icons",
                config.pairs <= GameLogic.ITEM_COUNT
            )
        }
    }

    // ------------------------------------------------------------ deck build

    @Test
    fun `deck contains every chosen item exactly twice`() {
        GameLogic.levels.forEach { config ->
            val deck = GameLogic.buildDeck(config.pairs, random = Random(config.level))
            assertEquals(config.pairs * 2, deck.size)

            val counts = deck.groupingBy { it.itemIndex }.eachCount()
            assertEquals(config.pairs, counts.size)
            counts.forEach { (item, count) ->
                assertEquals("item $item appeared $count times", 2, count)
            }
        }
    }

    @Test
    fun `deck card ids are unique and contiguous`() {
        val deck = GameLogic.buildDeck(8, random = Random(7))
        assertEquals(16, deck.size)
        assertEquals(deck.indices.toList(), deck.map { it.id }.sorted())
    }

    @Test
    fun `deck item indices are always inside the shipped item range`() {
        val deck = GameLogic.buildDeck(12, random = Random(1))
        deck.forEach { card ->
            assertTrue(card.itemIndex in 1..GameLogic.ITEM_COUNT)
        }
    }

    @Test
    fun `deck build clamps nonsense pair counts instead of throwing`() {
        assertEquals(2, GameLogic.buildDeck(0).size)
        assertEquals(2, GameLogic.buildDeck(-5).size)
        // More pairs than icons is clamped down to the number of icons.
        assertEquals(GameLogic.ITEM_COUNT * 2, GameLogic.buildDeck(99).size)
    }

    @Test
    fun `deck is shuffled but deterministic for a given seed`() {
        val a = GameLogic.buildDeck(10, random = Random(42)).map { it.itemIndex }
        val b = GameLogic.buildDeck(10, random = Random(42)).map { it.itemIndex }
        assertEquals(a, b)
    }

    // -------------------------------------------------------- unlock / clear

    @Test
    fun `level one is always unlocked and everything else starts locked`() {
        val empty = emptyMap<String, Int>()
        assertTrue(GameLogic.isLevelUnlocked(1, empty))
        (2..9).forEach { level ->
            assertFalse("level $level should be locked", GameLogic.isLevelUnlocked(level, empty))
        }
    }

    @Test
    fun `clearing a level unlocks exactly the next one`() {
        // Level 1 needs 2 pairs.
        val progress = mapOf("1" to 2)
        assertTrue(GameLogic.isLevelCompleted(1, progress))
        assertTrue(GameLogic.isLevelUnlocked(2, progress))
        assertFalse(GameLogic.isLevelUnlocked(3, progress))
    }

    @Test
    fun `a partial best does not complete or unlock`() {
        val progress = mapOf("1" to 1) // needs 2
        assertFalse(GameLogic.isLevelCompleted(1, progress))
        assertFalse(GameLogic.isLevelUnlocked(2, progress))
    }

    @Test
    fun `invalid level numbers are never unlocked`() {
        val full = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertFalse(GameLogic.isLevelUnlocked(0, full))
        assertFalse(GameLogic.isLevelUnlocked(10, full))
        assertFalse(GameLogic.isLevelUnlocked(-1, full))
    }

    @Test
    fun `completed level counting`() {
        assertEquals(0, GameLogic.completedLevelCount(emptyMap()))
        assertEquals(1, GameLogic.completedLevelCount(mapOf("1" to 2)))
        assertEquals(2, GameLogic.completedLevelCount(mapOf("1" to 2, "2" to 3)))
        // A non-contiguous entry still counts as a cleared level.
        assertEquals(2, GameLogic.completedLevelCount(mapOf("1" to 2, "5" to 6)))

        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertEquals(9, GameLogic.completedLevelCount(all))
    }

    @Test
    fun `completed level counting ignores junk keys and overshoot values`() {
        val junk = mapOf("" to 5, "abc" to 9, "0" to 4, "99" to 12, "1" to 500)
        assertEquals(1, GameLogic.completedLevelCount(junk))
    }

    @Test
    fun `highest unlocked level walks forward with progress`() {
        assertEquals(1, GameLogic.highestUnlockedLevel(emptyMap()))
        assertEquals(2, GameLogic.highestUnlockedLevel(mapOf("1" to 2)))
        assertEquals(3, GameLogic.highestUnlockedLevel(mapOf("1" to 2, "2" to 3)))

        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertEquals(9, GameLogic.highestUnlockedLevel(all))
    }
}
