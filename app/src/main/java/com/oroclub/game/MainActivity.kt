package com.oroclub.game

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.oroclub.game.data.GameRepository
import com.oroclub.game.ui.AetherRoot
import com.oroclub.game.ui.theme.Ink
import com.oroclub.game.ui.theme.OroClubTheme

/**
 * The single Activity. Portrait is locked in the manifest; edge-to-edge is
 * enabled here and every screen applies safe-drawing insets itself.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Oro Club is unconditionally dark, so the system bars are pinned to
        // light icons. The auto() default would follow the device's light/dark
        // setting and paint dark icons over the near-black artwork.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        // The Application owns the repository (AetherApp extends OroClubApp, so the
        // cast still resolves). If the process was recreated in a state where the
        // custom Application is missing (never expected, but cheap to survive), build
        // a local one rather than crashing.
        val repository: GameRepository =
            (application as? OroClubApp)?.repository ?: GameRepository(applicationContext)

        setContent {
            OroClubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    // AetherRoot decides between the game and the offer, then renders
                    // OroNavGraph itself on the White branch.
                    AetherRoot(repository = repository)
                }
            }
        }
    }
}
