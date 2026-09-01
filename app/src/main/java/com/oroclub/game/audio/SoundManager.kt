package com.oroclub.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.oroclub.game.R

/**
 * Tiny SFX player built on [SoundPool].
 *
 * Initialised once from the Application. Every call is defensive: if the pool
 * failed to build, a sample failed to load, or sound is switched off, playing is
 * a no-op instead of an exception.
 */
object SoundManager {

    enum class Sfx(@RawRes val resId: Int) {
        CLICK(R.raw.sfx_click),
        CORRECT(R.raw.sfx_correct),
        WRONG(R.raw.sfx_wrong),
        MATCH(R.raw.sfx_match),
        WIN(R.raw.sfx_win),
        LOSE(R.raw.sfx_lose)
    }

    @Volatile
    private var pool: SoundPool? = null

    private val soundIds = HashMap<Sfx, Int>(Sfx.entries.size)
    private val loaded = HashSet<Int>()

    @Volatile
    var isEnabled: Boolean = true
        private set

    /** Safe to call more than once; later calls are ignored. */
    @Synchronized
    fun init(context: Context) {
        if (pool != null) return
        val created = try {
            SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
        } catch (t: Throwable) {
            null
        } ?: return

        created.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                synchronized(loaded) { loaded.add(sampleId) }
            }
        }

        val app = context.applicationContext
        Sfx.entries.forEach { sfx ->
            val id = try {
                created.load(app, sfx.resId, 1)
            } catch (t: Throwable) {
                0
            }
            if (id != 0) soundIds[sfx] = id
        }
        pool = created
    }

    /** Mirrors the persisted setting; call at startup and whenever it changes. */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun play(sfx: Sfx) {
        if (!isEnabled) return
        val activePool = pool ?: return
        val id = soundIds[sfx] ?: return
        val ready = synchronized(loaded) { loaded.contains(id) }
        if (!ready) return
        try {
            activePool.play(id, VOLUME, VOLUME, 1, 0, 1f)
        } catch (t: Throwable) {
            // A dead pool must never crash a tap.
        }
    }

    fun click() = play(Sfx.CLICK)

    @Synchronized
    fun release() {
        try {
            pool?.release()
        } catch (t: Throwable) {
            // Nothing meaningful to do on teardown.
        }
        pool = null
        soundIds.clear()
        synchronized(loaded) { loaded.clear() }
    }

    private const val VOLUME = 0.85f
}
