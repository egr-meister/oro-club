package com.oroclub.game.integration

import com.oroclub.game.OroClubApp

/**
 * Application entry point for the integrated build.
 *
 * It extends [OroClubApp] rather than replacing it, so the game's own startup - the
 * DataStore repository and the SFX pool - runs first and unchanged.
 *
 * Order matters: storage before the manager (the manager reads `attributionSettled`),
 * the user agent before anything can probe, and AppsFlyer before OneSignal (the push
 * external id is the AppsFlyer UID).
 */
class AetherApp : OroClubApp() {

    override fun onCreate() {
        super.onCreate()
        IntegrationStorage.init(this)
        DeviceSignals.init(this)
        // Sampling takes a full second, so it has to begin at launch - starting it where
        // the URL is built would mean blocking there for the whole window.
        AccelerometerProbe.start(this)
        UserAgentProvider.init(this)
        AppsFlyerManager.init(this)
        OneSignalManager.init(this)
    }
}
