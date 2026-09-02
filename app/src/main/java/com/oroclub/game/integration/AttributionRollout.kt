package com.oroclub.game.integration

import kotlin.random.Random

/**
 * Decides whether this install initialises AppsFlyer at all.
 *
 * One install in [DEFAULT_HOLDBACK_EVERY_NTH] is held back: the SDK is never started,
 * so it collects nothing and sends nothing for that install. Pure Kotlin, so the rule
 * itself can be read and exercised without Android.
 *
 * The draw is random per install rather than a real counter: a device cannot know how
 * many installs came before it. Over enough installs that averages out to one in five,
 * but any individual run of five is not guaranteed to contain exactly one held-back
 * install - only a server-side counter could promise that.
 */
object AttributionRollout {

    /** 5 -> one install in five is held back. 0 or less -> the holdback is off. */
    const val DEFAULT_HOLDBACK_EVERY_NTH = 5

    /**
     * @param roll a value in `0 until everyNth`.
     * @return true when this install should run AppsFlyer normally.
     */
    fun isEnabled(roll: Int, everyNth: Int = DEFAULT_HOLDBACK_EVERY_NTH): Boolean {
        if (everyNth <= 0) return true
        // floorMod by hand so a negative roll cannot invert the answer.
        return ((roll % everyNth) + everyNth) % everyNth != 0
    }

    /** Draws once. The result belongs in storage - never re-draw on a later launch. */
    fun decide(
        everyNth: Int = DEFAULT_HOLDBACK_EVERY_NTH,
        random: Random = Random.Default
    ): Boolean {
        if (everyNth <= 0) return true
        return isEnabled(random.nextInt(everyNth), everyNth)
    }
}
