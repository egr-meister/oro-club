package com.oroclub.game

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oroclub.game.data.GameRepository
import com.oroclub.game.integration.DeepLinkRouter
import com.oroclub.game.integration.PushPayload
import com.oroclub.game.integration.PushService
import com.oroclub.game.ui.AetherRoot
import com.oroclub.game.ui.theme.Ink
import com.oroclub.game.ui.theme.OroClubTheme
import com.oroclub.game.ui.theme.ThemeMode
import kotlinx.coroutines.flow.map

/**
 * The single Activity.
 *
 * AppCompatActivity (not ComponentActivity) is required for
 * AppCompatDelegate.setApplicationLocales to survive a restart below API 33 - that is the
 * one reason appcompat is a dependency.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // System bars pinned to light icons: the artwork is near-black, and the auto()
        // default would paint dark icons over it in a dark environment.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        hideSystemBars()

        // A tap on a push notification lands here; deliver its link to the WebView.
        consumeIntent(intent)
        requestNotificationPermission()

        val repository: GameRepository =
            (application as? OroClubApp)?.repository ?: GameRepository(applicationContext)

        val themeModeFlow = repository.data.map { ThemeMode.fromStorage(it.settings.themeMode) }

        setContent {
            val themeMode by themeModeFlow.collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)
            OroClubTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    AetherRoot(repository = repository)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    /**
     * Turns a push tap into a deep link, exactly once. The mark goes on the intent itself,
     * not a field on the Activity, so a recreation cannot replay the same push.
     */
    private fun consumeIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(EXTRA_CONSUMED, false)) return
        // A launcher intent also has an action and a category - require our own marker or a
        // url extra (the latter is how the system delivers a background notification's data).
        val fromPush = intent.getBooleanExtra(PushService.EXTRA_FROM_PUSH, false) ||
            intent.hasExtra(PushPayload.KEY_URL)
        if (!fromPush) return
        intent.putExtra(EXTRA_CONSUMED, true)
        val url = intent.getStringExtra(PushPayload.KEY_URL)
        if (!url.isNullOrBlank()) DeepLinkRouter.handle(url)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            // Non-blocking: the FCM token arrives regardless, so a refusal breaks nothing.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    private companion object {
        const val EXTRA_CONSUMED = "oro_push_consumed"
    }
}
