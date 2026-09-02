package com.oroclub.game.integration

import android.app.Application
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.oroclub.game.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Push notifications.
 *
 * Initialised after AppsFlyer so that the external id it logs in with - the AppsFlyer UID -
 * already exists whenever the SDK managed to produce one. When the UID arrives later,
 * AppsFlyerManager calls [syncExternalId] again.
 */
object OneSignalManager {

    private const val TAG = "OneSignalManager"

    @Volatile
    private var initialised = false

    fun init(app: Application) {
        val appId = BuildConfig.ONESIGNAL_APP_ID
        if (appId.isBlank()) {
            // Initialising with an empty id makes the SDK throw at startup, so a build
            // without the key simply ships without push.
            Log.w(TAG, "ONESIGNAL_APP_ID is empty - push disabled for this build")
            return
        }

        runCatching {
            OneSignal.initWithContext(app, appId)
            initialised = true

            // Written as an object expression rather than a lambda: the listener is a
            // Kotlin interface, so SAM conversion is not guaranteed across SDK versions.
            OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                override fun onClick(event: INotificationClickEvent) {
                    // The payload carries the destination as additionalData.url; the
                    // router holds it until the root composable is listening.
                    val url = event.notification.additionalData?.optString("url").orEmpty()
                    if (url.isNotBlank()) {
                        DeepLinkRouter.handle(url)
                    }
                }
            })

            // requestPermission is a suspend function in OneSignal 5.x, so it cannot be
            // called from onCreate directly.
            CoroutineScope(Dispatchers.Main).launch {
                runCatching { OneSignal.Notifications.requestPermission(true) }
                    .onFailure { Log.w(TAG, "Notification permission request failed", it) }
            }

            syncExternalId()
        }.onFailure { Log.w(TAG, "OneSignal init failed", it) }
    }

    /**
     * Ties the push subscription to the AppsFlyer UID, so a campaign can address the same
     * install in both systems. Safe to call repeatedly: the UID is stable once stored.
     */
    fun syncExternalId() {
        if (!initialised) return
        val externalId = IntegrationStorage.appsFlyerId
        if (externalId.isNullOrBlank()) return
        runCatching { OneSignal.login(externalId) }
            .onFailure { Log.w(TAG, "OneSignal.login failed", it) }
    }
}
