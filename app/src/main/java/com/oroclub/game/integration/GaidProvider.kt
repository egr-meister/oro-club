package com.oroclub.game.integration

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * The Google advertising id (GAID / gps_adid).
 *
 * On Android it is the primary key the tracker matches S2S postbacks on (the Keitaro
 * campaign maps it to sub_id_7); Adjust's own adid is only the backup key. So it has to
 * ride in the single click alongside everything else.
 *
 * AdvertisingIdClient.getAdvertisingIdInfo blocks and must not run on the main thread, so
 * it is fetched on its own thread at launch (like the push token and the accelerometer)
 * and awaited briefly when the URL is built.
 *
 * Needs the com.google.android.gms.permission.AD_ID permission (declared in the manifest)
 * to return a real id on targetSdk 33+; without it the id comes back zeroed.
 */
object GaidProvider {

    private const val TAG = "GaidProvider"
    private const val AWAIT_TIMEOUT_SECONDS = 3L
    private val ready = CountDownLatch(1)

    @Volatile
    private var gaid: String = ""

    fun init(app: Context) {
        val appContext = app.applicationContext
        Thread {
            runCatching {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(appContext)
                val id = info.id
                // A limit-ad-tracking device returns an all-zero id; keep whatever came
                // back (zeros included) - "zeroed" and "not read" are different answers to
                // the offer side.
                if (!id.isNullOrBlank()) gaid = id
            }.onFailure {
                // Play Services missing, or a vendor ROM that throws: leave gaid empty.
                Log.w(TAG, "Could not read the advertising id", it)
            }
            ready.countDown()
        }.apply { isDaemon = true; name = "gaid-probe" }.start()
    }

    /** Background threads only - blocks up to [AWAIT_TIMEOUT_SECONDS]. */
    fun awaitGaid(): String {
        runCatching { ready.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS) }
            .onFailure {
                Thread.currentThread().interrupt()
                Log.w(TAG, "Interrupted while waiting for the advertising id", it)
            }
        return gaid
    }
}
