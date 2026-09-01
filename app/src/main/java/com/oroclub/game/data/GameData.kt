package com.oroclub.game.data

import kotlinx.serialization.Serializable

/**
 * Persisted settings. Every field has a default so that older or newer payloads
 * always decode.
 */
@Serializable
data class Settings(
    val soundEnabled: Boolean = true
)

/**
 * The single persisted document of the game.
 *
 * @param quizBest  best score (0..10) per quiz category id.
 * @param pairsBest best number of matched pairs per level (key = level number as String).
 * @param settings  user settings.
 *
 * Maps are keyed by String so the JSON stays stable and forward compatible.
 */
@Serializable
data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings()
)
