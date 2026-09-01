package com.oroclub.game.ui

import androidx.annotation.DrawableRes
import com.oroclub.game.R

/**
 * THE single place where drawable resources are referenced.
 *
 * Drop replacement artwork into `res/drawable-nodpi/` using exactly these file
 * names and nothing in the code has to change:
 *
 *   bg_main.jpg        full-screen background (opaque JPG)
 *   logo.png           app wordmark
 *   mascot_a.png       joker mascot, pose A (full body, transparent)
 *   mascot_b.png       joker mascot, pose B (full body, transparent)
 *   btn_menu.png       ornate framed button plate  (aspect [MENU_PLATE_ASPECT])
 *   btn_plate.png      simple in-panel button plate (aspect [PLATE_ASPECT])
 *   plate_round.png    round bubble used for level nodes and card backs
 *   icon_back.png      round back icon
 *   icon_pause.png     round pause icon
 *   item_01..item_12   the 12 themed match icons
 *
 * Note: when you replace `bg_main.jpg` with a `.png`, delete the old `.jpg`
 * first — Android resolves a drawable by name, and two files with the same name
 * and different extensions is a duplicate-resource build error.
 */
object Assets {

    @DrawableRes val background: Int = R.drawable.bg_main
    @DrawableRes val logo: Int = R.drawable.logo
    @DrawableRes val mascotA: Int = R.drawable.mascot_a
    @DrawableRes val mascotB: Int = R.drawable.mascot_b
    @DrawableRes val menuPlate: Int = R.drawable.btn_menu
    @DrawableRes val buttonPlate: Int = R.drawable.btn_plate
    @DrawableRes val roundPlate: Int = R.drawable.plate_round
    @DrawableRes val iconBack: Int = R.drawable.icon_back
    @DrawableRes val iconPause: Int = R.drawable.icon_pause

    /** Width / height of `btn_menu.png`. Used with Modifier.aspectRatio(). */
    const val MENU_PLATE_ASPECT: Float = 1000f / 333f

    /** Width / height of `btn_plate.png`. */
    const val PLATE_ASPECT: Float = 1000f / 333f

    /** Width / height of `mascot_a.png`. */
    const val MASCOT_ASPECT: Float = 740f / 980f

    /** Width / height of `logo.png`. */
    const val LOGO_ASPECT: Float = 1f

    private val items: List<Int> = listOf(
        R.drawable.item_01,
        R.drawable.item_02,
        R.drawable.item_03,
        R.drawable.item_04,
        R.drawable.item_05,
        R.drawable.item_06,
        R.drawable.item_07,
        R.drawable.item_08,
        R.drawable.item_09,
        R.drawable.item_10,
        R.drawable.item_11,
        R.drawable.item_12
    )

    /** Human-readable names, used for content descriptions of the match icons. */
    private val itemNames: List<String> = listOf(
        "Joker mask",
        "Jester's crown",
        "Golden die",
        "Lucky horseshoe",
        "Club gem",
        "Signet ring",
        "Golden key",
        "Treasure chest",
        "Winner's cup",
        "Hourglass",
        "Fortune star",
        "Gilded lantern"
    )

    val itemCount: Int get() = items.size

    /** @param index 1-based item number; out-of-range values wrap safely. */
    @DrawableRes
    fun item(index: Int): Int = items[safeIndex(index)]

    fun itemName(index: Int): String = itemNames[safeIndex(index)]

    private fun safeIndex(index: Int): Int {
        if (items.isEmpty()) return 0
        val zeroBased = index - 1
        val wrapped = ((zeroBased % items.size) + items.size) % items.size
        return wrapped
    }
}
