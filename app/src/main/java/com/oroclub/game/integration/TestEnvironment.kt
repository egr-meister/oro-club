package com.oroclub.game.integration

/** sub14: is this install running somewhere that is not a real user's phone. */
object TestEnvironment {

    data class Snapshot(
        val firebaseTestLab: String? = null,
        val instrumentationPresent: Boolean = false,
        val debuggerConnected: Boolean = false,
        val debuggable: Boolean = false,
        val hardware: String = "",
        val product: String = "",
        val model: String = "",
        val manufacturer: String = "",
        val brand: String = "",
        val device: String = "",
        val fingerprint: String = ""
    )

    private val HARDWARE_MARKERS = listOf(
        "goldfish", "ranchu", "vbox", "android_x86", "gce_x86", "cutf", "cuttlefish"
    )

    // Without a trailing underscore - the "<marker>_" prefix is applied at the comparison.
    // "sdk" is load-bearing: the current Android Studio AVD reports
    // product = "sdk_gphone64_arm64", which matches only through "sdk" + the "_" rule.
    // Removing it as "too broad" silently switches emulator detection off.
    private val PRODUCT_MARKERS = listOf(
        "sdk", "google_sdk", "vbox86p", "emulator", "simulator"
    )

    private val MODEL_MARKERS = listOf(
        "google_sdk", "emulator", "android sdk built for", "sdk_gphone"
    )

    fun isTestEnvironment(s: Snapshot): Boolean =
        s.firebaseTestLab == "true" ||
            s.instrumentationPresent ||
            s.debuggerConnected ||
            s.debuggable ||
            isEmulator(s)
    // Deliberately NOT here (see device-signals.md): Settings.Global.ADB_ENABLED fires on
    // every user who ever opened developer options, and a test-keys fingerprint on every
    // AOSP-derived ROM - both are real people, and flagging them as test traffic hands the
    // offer side a reason to drop paying users.

    fun isEmulator(s: Snapshot): Boolean {
        val hardware = s.hardware.lowercase()
        val product = s.product.lowercase()
        val model = s.model.lowercase()
        val fingerprint = s.fingerprint.lowercase()

        if (HARDWARE_MARKERS.any { hardware.contains(it) }) return true

        // Equality or a "<marker>_" prefix, never a bare prefix: a real device whose
        // product is "sdkfusion" would otherwise be written off as an emulator, and every
        // install from it treated as fake traffic.
        if (PRODUCT_MARKERS.any { product == it || product.startsWith("${it}_") }) return true

        if (MODEL_MARKERS.any { model.contains(it) }) return true
        if (s.manufacturer.lowercase().contains("genymotion")) return true

        if (fingerprint.startsWith("generic") || fingerprint.contains("/sdk_")) return true

        // Both, not either - a single "generic" is common enough on real hardware.
        if (s.brand.lowercase().startsWith("generic") &&
            s.device.lowercase().startsWith("generic")
        ) return true

        return false
    }
}
