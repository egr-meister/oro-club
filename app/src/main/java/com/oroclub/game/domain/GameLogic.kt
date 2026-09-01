package com.oroclub.game.domain

import kotlin.random.Random

/** Configuration of one Matching Pairs level. */
data class LevelConfig(
    val level: Int,
    val pairs: Int,
    val columns: Int,
    val seconds: Int
)

/** One face-down card on the board. [itemIndex] is 1..12 and maps to item_XX art. */
data class CardModel(
    val id: Int,
    val itemIndex: Int
)

/**
 * Pure, context-free game rules. No Android types here so the whole file is
 * covered by fast JVM unit tests.
 */
object GameLogic {

    /** Total number of distinct themed match icons shipped as item_01..item_12. */
    const val ITEM_COUNT: Int = 12

    /** Number of Matching Pairs levels. */
    const val LEVEL_COUNT: Int = 9

    val levels: List<LevelConfig> = listOf(
        LevelConfig(level = 1, pairs = 2, columns = 2, seconds = 60),
        LevelConfig(level = 2, pairs = 3, columns = 2, seconds = 70),
        LevelConfig(level = 3, pairs = 4, columns = 2, seconds = 70),
        LevelConfig(level = 4, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 5, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 6, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 7, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 8, pairs = 10, columns = 4, seconds = 110),
        LevelConfig(level = 9, pairs = 12, columns = 4, seconds = 120)
    )

    /** @return the config for [level], or null when the level number is invalid. */
    fun levelConfig(level: Int): LevelConfig? = levels.firstOrNull { it.level == level }

    fun isValidLevel(level: Int): Boolean = level in 1..LEVEL_COUNT

    /**
     * Builds a shuffled deck containing exactly [pairs] distinct item indices,
     * each appearing exactly twice.
     *
     * [pairs] is clamped into 1..[itemCount] so a bad caller can never produce a
     * deck with duplicated-beyond-two or missing cards.
     */
    fun buildDeck(
        pairs: Int,
        itemCount: Int = ITEM_COUNT,
        random: Random = Random.Default
    ): List<CardModel> {
        val safeItemCount = itemCount.coerceAtLeast(1)
        val safePairs = pairs.coerceIn(1, safeItemCount)
        val chosen = (1..safeItemCount).shuffled(random).take(safePairs)
        val doubled = chosen.flatMap { listOf(it, it) }.shuffled(random)
        return doubled.mapIndexed { index, itemIndex -> CardModel(id = index, itemIndex = itemIndex) }
    }

    /** A level counts as completed when every one of its pairs has been matched. */
    fun isLevelCompleted(level: Int, pairsBest: Map<String, Int>): Boolean {
        val config = levelConfig(level) ?: return false
        val best = pairsBest[level.toString()] ?: 0
        return best >= config.pairs
    }

    /** Level 1 is always playable; every later level needs the previous cleared. */
    fun isLevelUnlocked(level: Int, pairsBest: Map<String, Int>): Boolean {
        if (!isValidLevel(level)) return false
        if (level == 1) return true
        return isLevelCompleted(level - 1, pairsBest)
    }

    /** How many of the [LEVEL_COUNT] levels have been fully cleared. */
    fun completedLevelCount(pairsBest: Map<String, Int>): Int =
        levels.count { isLevelCompleted(it.level, pairsBest) }

    /** Highest level the player may currently open. */
    fun highestUnlockedLevel(pairsBest: Map<String, Int>): Int =
        (LEVEL_COUNT downTo 1).firstOrNull { isLevelUnlocked(it, pairsBest) } ?: 1
}
