package com.oroclub.game.integration

import com.oroclub.game.OroClubApp

/**
 * Application entry point for the integrated build.
 *
 * It extends [OroClubApp] rather than replacing it, so the game's own startup - the
 * DataStore repository and the SFX pool - runs first and unchanged.
 *
 * Order matters: storage first (everything else reads from it); the accelerometer and the
 * push token are started here, not where the URL is built, because each needs a window and
 * building the URL would otherwise block for it.
 */
class AetherApp : OroClubApp() {

    override fun onCreate() {
        super.onCreate()
        IntegrationStorage.init(this)
        DeviceSignals.init(this)
        // Sampling takes a full second, so it has to begin at launch.
        AccelerometerProbe.start(this)
        // The GAID read blocks on Play Services, so start it at launch too.
        GaidProvider.init(this)
        AdjustManager.init(this)
        // The token must exist before the single click, so request it at launch.
        PushManager.init(this)
    }
}
