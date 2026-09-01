package com.oroclub.game.data

import kotlinx.serialization.json.Json

/**
 * Context-free encode/decode for [GameData]. Deliberately has no Android
 * dependency so it can be covered by plain JVM unit tests.
 *
 * Decoding NEVER throws: null, blank, malformed, truncated or foreign JSON all
 * fall back to [GameData] defaults.
 */
object GameSerializer {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    fun encode(data: GameData): String =
        try {
            json.encodeToString(GameData.serializer(), data)
        } catch (t: Throwable) {
            // Encoding a plain data class cannot realistically fail, but a
            // corrupted in-memory map must never take the app down.
            EMPTY_JSON
        }

    fun decode(raw: String?): GameData {
        if (raw.isNullOrBlank()) return GameData()
        return try {
            json.decodeFromString(GameData.serializer(), raw)
        } catch (t: Throwable) {
            GameData()
        }
    }

    private const val EMPTY_JSON = "{}"
}
