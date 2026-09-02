package com.oroclub.game.integration

/**
 * sub15: why the offer URL carries the attribution it carries.
 *
 * Without it every URL that arrives without campaign data looks the same on the tracker,
 * and four different causes are indistinguishable: this install was held back, the build
 * shipped without a dev key, AppsFlyer had not answered yet, or it answered with nothing.
 * Those call for four different reactions, so they get four different values.
 */
enum class AttributionStatus(val value: String) {

    /** AppsFlyer ran and reported attribution - the normal case. */
    OK("ok"),

    /** AppsFlyer ran and answered, but reported no parameters at all (organic). */
    EMPTY("empty"),

    /** AppsFlyer ran but had not answered by the time the URL was built. */
    PENDING("pending"),

    /** [AttributionRollout] held this install back; the SDK was never started. */
    HOLDBACK("holdback"),

    /** The build shipped with a blank APPSFLYER_DEV_KEY - a configuration mistake. */
    NO_KEY("nokey");

    companion object {

        /**
         * Pure decision, so the precedence can be read in one place.
         *
         * Order matters: a held-back install also looks "resolved with no data", and a
         * keyless build looks the same again. The cause has to win over the symptom.
         */
        fun of(
            attributionEnabled: Boolean,
            devKeyPresent: Boolean,
            resolved: Boolean,
            hasAttribution: Boolean
        ): AttributionStatus = when {
            !attributionEnabled -> HOLDBACK
            !devKeyPresent -> NO_KEY
            !resolved -> PENDING
            hasAttribution -> OK
            else -> EMPTY
        }
    }
}
