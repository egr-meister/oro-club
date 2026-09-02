package com.oroclub.game

import android.app.Application
import com.oroclub.game.audio.SoundManager
import com.oroclub.game.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Composition root. The repository is created exactly once here and handed to
 * simple ViewModel factories — no DI framework is used anywhere in the project.
 *
 * Open because the integration layer extends it: `AetherApp` adds the attribution and
 * routing startup on top of this one, rather than replacing it.
 */
open class OroClubApp : Application() {

    lateinit var repository: GameRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        repository = GameRepository(this)
        SoundManager.init(this)

        // Keep the audio switch in sync with the persisted setting, at startup
        // and live for the whole process lifetime.
        appScope.launch {
            repository.data
                .map { it.settings.soundEnabled }
                .distinctUntilChanged()
                .collect { enabled -> SoundManager.setEnabled(enabled) }
        }
    }

    override fun onTerminate() {
        SoundManager.release()
        super.onTerminate()
    }
}
