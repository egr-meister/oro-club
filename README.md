# Oro Club — Golden Joker Club

A casual, single-player Android game — a joker-themed quiz and a nine-level
Matching Pairs memory game, built with Kotlin and Jetpack Compose — plus a
White/Black integration layer that decides at startup which of the two the
install sees.

* Application ID: `com.oroclub.game`
* `minSdk 24` · `compileSdk 37` · `targetSdk 37` · JDK 17 · portrait only
* **White** — the game. Entirely local: no account, no ads, no analytics inside
  it, no backend, no cloud. Section 3 describes its storage.
* **Black** — a remote offer in a WebView, reached only when the tracker's answer
  to the campaign link says so. Section 14 describes the whole layer.

> **Not gambling.** The game is a for-fun quiz and memory game with a joker /
> playing-card / luck *theme*. There is no wagering, no real-money play, no
> prizes and no simulated betting of any kind inside the app.

---

## 1. Features

**Quiz**

* 3 themed decks × 10 questions, one correct answer each.
* One question at a time with a progress bar and an `n/10` counter.
* Tapping an answer reveals it immediately — correct in green with a **✓**,
  wrong in red with a **✗** — then auto-advances after ~0.75 s.
* Best score per deck is stored locally.
* Pause overlay: Continue / Restart / Menu.
* Results: `Score: X/10` with Continue / Restart / Menu.

**Matching Pairs** (never called "Puzzles")

| Level | Pairs | Columns | Seconds |
|------:|------:|--------:|--------:|
| 1 | 2 | 2 | 60 |
| 2 | 3 | 2 | 70 |
| 3 | 4 | 2 | 70 |
| 4 | 6 | 3 | 80 |
| 5 | 6 | 3 | 80 |
| 6 | 8 | 4 | 100 |
| 7 | 8 | 4 | 100 |
| 8 | 10 | 4 | 110 |
| 9 | 12 | 4 | 120 |

* The deck is built from the 12 themed match icons: `pairs` distinct icons,
  each duplicated once, then shuffled.
* Flip two cards. A match stays face up; a mismatch flips back after a short
  delay.
* Per-level countdown that pauses with the pause overlay.
* Clear every pair before time runs out → **Well Done**. Time out → **Out of
  Time**.
* Best matched-pairs count is stored per level; a level unlocks once the
  previous one is cleared (level 1 is always unlocked).

**Screens** — Main, Menu, Quiz categories, Quiz, Quiz results, Levels (9 bubbles
+ Game Rules), Pairs game, Pairs results, Results summary, Settings, Game Rules.

---

## 2. What talks to the network, and what does not

The game itself — every screen in section 1 — is entirely local. It reads and
writes one DataStore file and nothing else; no screen, ViewModel or repository
in the game makes a network call.

Three permissions exist for the integration layer only:

| Permission | Used by |
|---|---|
| `INTERNET` | Adjust attribution, the offer WebView, push |
| `ACCESS_NETWORK_STATE` | the SDKs' own connectivity checks |
| `POST_NOTIFICATIONS` | the Android 13+ push permission prompt |

There are still no services, no `WorkManager`, no alarms, and no keep-awake
flags.

### Missing integrations (deliberate)

No networking library (Retrofit / OkHttp / Ktor — the offer is opened by the
WebView itself, there is no HTTP probe), no ads SDK, no payment or billing
library, no maps, no login, no DI framework, no Room. Firebase Cloud Messaging is
present for push; attribution is Adjust.

### Privacy

Game progress never leaves the device. The integration layer is a different
matter and should be described accurately in the store listing: Adjust receives
install attribution, Firebase Cloud Messaging delivers push, and on the Black
branch the offer page is a third-party site inside a WebView with its own data
practices.

---

## 3. Local storage

All state is one DataStore Preferences file, `oro_club_progress`, holding a
single JSON string under one key.

```kotlin
@Serializable
data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),   // best score per category id
    val pairsBest: Map<String, Int> = emptyMap(),  // best matched pairs per level
    val settings: Settings = Settings()
)

@Serializable
data class Settings(val soundEnabled: Boolean = true)
```

Every field has a default, so an older or newer document always decodes.
Encoding and decoding live in `GameSerializer`, a context-free object
(`ignoreUnknownKeys = true`) that is directly unit-testable.

**Reads never crash.** Empty storage, a missing key, an empty string, truncated
or corrupted JSON, unknown keys, missing fields — every one of them resolves to
`GameData()` defaults. `GameRepository` additionally catches `IOException` from
DataStore and emits empty preferences instead of propagating.

Repository API: observe `data: Flow<GameData>`, `saveQuizBest`,
`savePairsBest`, `setSoundEnabled`, `resetAll`. Bests are only written when they
actually improve.

---

## 4. Visual concept — "Golden Joker Club"

### Palette

The palette below is matched to the shipped artwork (a deep-navy night sky with
gold filigree and cyan gem glow). Change the values in
`ui/theme/Color.kt` and `res/values/colors.xml` to reskin the app.

| Token | HEX | Usage |
|---|---|---|
| `Gold` | `#F5C542` | primary accent — borders, titles, active states, icons |
| `GoldDark` | `#B8860B` | dark stop of gold gradients, dividers, disabled gold |
| `GoldSoft` | `#FFE9A8` | light stop of gold gradients, highlights |
| `DeepRoyal` | `#0A142E` | panel gradient top, large fills behind content |
| `Panel` | `#16264D` | panel gradient bottom, card faces |
| `Ink` | `#01081A` | page ground beneath the background image |
| `NearBlack` | `#05080F` | scrims, progress-bar tracks |
| `Gem` | `#4FC3F7` | secondary accent — countdown, sub-labels |
| `CorrectGreen` | `#2F8F5B` | correct answer / matched pair (always with a ✓) |
| `WrongRed` | `#B73A3A` | wrong answer / time out (always with a ✗) |
| `TextPrimary` | `#FFFFFF` | button labels and body copy |
| `TextMuted` | `#C9D3E8` | secondary copy |

The original brief's purple (`#2A0A4A` / `#3A0F6E`) was replaced with navy once
the real artwork arrived, because the supplied background and icons are
navy-and-gold. Swap the two tokens back if you re-skin.

### Typography

Material 3 `Typography` built on the platform default font family. No font files
are bundled, so the APK stays small and the user's font-scale setting is
honoured throughout.

### The main visual object and the home screen

The home screen is deliberately **not** the generic
mascot → title → stats-card → button-stack dashboard. It is composed like a club
poster in four layers:

1. the joker mascot standing full height with his feet cropped by the bottom
   edge,
2. the ORO Club wordmark high in the night sky with a letterspaced tagline,
3. a single small gold **club token** in the top-right corner carrying
   `cleared / 9` — progress without a statistics card,
4. one ornate `ENTER THE CLUB` plate overlapping the mascot's lower body, so the
   layers read as depth rather than as a list.

### Buttons and panels

* Every button is an **image plate with the label drawn on top in white bold
  text**. One plate is reused everywhere: `btn_menu` (ornate frame) for
  main-menu-style buttons, `btn_plate` (simple) inside panels.
* The ornate plate is tall, so menu buttons are constrained to ~0.7 of the
  screen width via `Modifier.aspectRatio(Assets.MENU_PLATE_ASPECT)` and the
  screen scrolls when it has to. The white label is centred inside the plate.
* **Modal panels (pause / results / rules) are drawn in Compose** — a rounded
  rectangle with a vertical gradient and a gold gradient border. A decorative
  frame image is never stretched around variable content, so nothing distorts or
  overflows. Buttons stay inside the panel padding by construction (fractional
  widths).
* Modals and results pages use `CenteredScrollContent`: centred while they fit,
  scrollable the moment they do not.

### App icon

The ORO Club orbit mark (a gold planet with a crown and a ring) on deep navy.

* Legacy density icons: `mipmap-{mdpi…xxxhdpi}/ic_launcher.png` and
  `ic_launcher_round.png` (48 / 72 / 96 / 144 / 192 px).
* Adaptive icon: `mipmap-anydpi-v26/ic_launcher.xml` with a solid
  `#0A142E` background, `drawable-xxxhdpi/ic_launcher_foreground.png` (432 px,
  logo inside the 66 % safe zone) and a **themed (monochrome)** vector layer,
  `drawable/ic_launcher_monochrome.xml`.
* The default Android icon is not used anywhere.

### Splash

`Theme.OroClub.Splash` paints the theme background colour (`#01081A`) with the
app mark centred at a fixed 160 dp — a layer-list, not a full-screen bitmap, so
the first frame is instant and already on-brand. No white flash, no heavy asset.

### Accessibility

* All text is white or `#C9D3E8` on navy/black — well above 4.5:1.
* Every interactive element is at least 48 dp (`MinTouchTarget`), including the
  level bubbles and the back/pause tokens.
* Content descriptions on every image button, level bubble, card and summary
  row; decorative images pass `contentDescription = null`.
* Colour is never the only signal: correct/wrong answers also carry ✓ / ✗,
  locked levels carry a padlock and the word `LOCKED`, and win/lose is stated in
  words ("Well Done" / "Out of Time").
* Font scaling is honoured; every long screen scrolls, and plate labels are
  sized from the plate width so they cannot overflow the artwork.
* Complex composites (`RoundPlate`, cards, summary rows) use
  `clearAndSetSemantics` so a screen reader announces one sentence instead of
  every glyph.

---

## 5. Tech stack

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.12.01) · Material 3 ·
Navigation Compose 2.8.5 · AndroidX Lifecycle 2.8.7 (ViewModel +
`collectAsStateWithLifecycle`) · Coroutines 1.9.0 · Flow ·
DataStore Preferences 1.1.1 · kotlinx-serialization-json 1.7.3 ·
Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`) ·
AGP 8.14.0 · Gradle 8.14.3 · JDK 17.

Integration layer: Adjust `adjust-android` 5.8.0 · Firebase Cloud Messaging
(Firebase BOM 34.19.0) · `android.webkit.WebView`.

Every package the code imports is a **direct** catalog dependency, including
`androidx.compose.foundation`, `androidx.compose.animation` and
`androidx.annotation`.

---

## 6. Architecture

Simple MVVM, no DI framework.

```
AetherApp (Application)      integration startup, extends OroClubApp
  └─ OroClubApp (Application)
       └─ GameRepository      created once, backed by DataStore Preferences
            └─ GameSerializer context-free JSON encode/decode (unit-tested)

ui/vm
  ProgressViewModel   ProgressUiState  (menu, levels, results, settings)
  QuizViewModel       QuizUiState
  PairsViewModel      PairsUiState
        ↑ all expose immutable state via StateFlow
        ↑ screens collect with collectAsStateWithLifecycle

domain
  GameLogic     levels, deck building, unlock/completion — context-free, tested
  QuizContent   the 3 × 10 questions — plain data, replaceable

ui/AetherRoot        startup gate: deep link > Black WebView > White game
ui/nav/OroNavGraph   Navigation Compose graph with typed arguments (the White branch)
integration/         attribution, offer URL, routing, WebView, push (section 13)
```

ViewModels are created through small `ViewModelProvider.Factory` classes that
receive the single repository instance from the Application.

Navigation is defensive: every destination validates its arguments (unknown
category id, level outside 1..9, negative or oversized counters) and renders a
friendly `FallbackScreen` with a working Back instead of crashing. Missing
arguments, deleted progress and process recreation are all survivable.

---

## 7. Assets

All artwork lives in `app/src/main/res/drawable-nodpi/` and is referenced from
exactly one place, `ui/Assets.kt`. Drop in replacements with the **same file
names** and no code changes are needed.

| File | Purpose |
|---|---|
| `bg_main.jpg` | full-screen background (opaque JPG) |
| `logo.png` | ORO Club wordmark |
| `mascot_a.png`, `mascot_b.png` | joker mascot, full body |
| `btn_menu.png` | ornate framed button plate |
| `btn_plate.png` | simple in-panel button plate |
| `plate_round.png` | round bubble: level nodes and card backs |
| `icon_back.png`, `icon_pause.png` | round icon buttons |
| `item_01.png` … `item_12.png` | the 12 themed match icons |

Match icons, in order: joker mask, jester's crown, golden die, lucky horseshoe,
club gem, signet ring, golden key, treasure chest, winner's cup, hourglass,
fortune star, gilded lantern. Their human-readable names are in `Assets` and are
used as content descriptions.

If you replace `bg_main.jpg` with a PNG, **delete the `.jpg` first** — Android
resolves drawables by name, and two files with the same name and different
extensions is a duplicate-resource build error.

`MENU_PLATE_ASPECT`, `PLATE_ASPECT`, `MASCOT_ASPECT` and `LOGO_ASPECT` in
`Assets.kt` are the width / height of the shipped art; update them if your
replacements have different proportions.

### Audio

`SoundManager` (a Kotlin object, initialised from `OroClubApp`) plays short SFX
through `SoundPool`: `click`, `correct`, `wrong`, `match`, `win`, `lose`. The
shipped files in `res/raw/` are simple synthesized tones — replace
`sfx_click.wav`, `sfx_correct.wav`, `sfx_wrong.wav`, `sfx_match.wav`,
`sfx_win.wav`, `sfx_lose.wav` with real audio using the same names.

The Settings sound toggle is persisted in DataStore, and the Application
collects that setting so `SoundManager.isEnabled` is synced at startup and live.
Every playback path is defensive: if the pool failed to build or a sample failed
to load, playing is a no-op.

---

## 8. Build and run

### Open in Android Studio

1. Android Studio Ladybug (2024.2.1) or newer.
2. `File ▸ Open…` and pick the project root.
3. The Gradle wrapper is committed and pinned to **Gradle 8.11.1**, so `./gradlew`
   works out of the box. If you ever need to regenerate it:
   ```bash
   gradle wrapper --gradle-version 8.11.1
   ```
4. Make sure the project JDK is **17** (`Settings ▸ Build Tools ▸ Gradle ▸
   Gradle JDK`) and that **SDK Platform 36** and **Build Tools 36.0.0** are
   installed in the SDK Manager.

### Debug build

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug variant uses the application id `com.oroclub.game.debug`, so it can be
installed next to a release build.

### Unit tests

```bash
./gradlew testDebugUnitTest
```

Covers deck building (exact pair counts, unique ids, clamped inputs,
deterministic shuffling), level unlock and completion, completed-level counting
with junk keys, quiz content validation, and safe JSON handling
(null / empty / corrupted / unknown keys / missing fields / round trip).

The integration layer adds pure-JVM tests for `CampaignParser`,
`OfferUrlBuilder`, `RoutingRules`, `OfferCache`, `UrlSchemes` and
`DeepLinkRouter` — campaign explosion into `sub1..subN`, the exact reference URL
(path segment + every Adjust parameter + the original `campaign` + subs + `adid`,
and no `%2526`), the empty-`sub1` case, the offer-cache TTL, and the
404 → White / anything else → Black rule.

### 16 KB memory page size

The app's own code is pure Kotlin/Compose with no native libraries. Adjust and
Firebase Cloud Messaging are Java/Kotlin SDKs and do not ship `.so` files either,
so there is nothing to re-align — but since that is now a property of third-party
artifacts rather than of this source tree, verify it on every release:

```bash
unzip -l app/build/outputs/bundle/release/app-release.aab | grep -E '\.so$' || echo "no native libraries"
```

An empty result is the expected outcome.

---

## 9. Release, signing and R8

### Generate a PKCS12 keystore

```bash
keytool -genkeypair \
  -v -storetype PKCS12 \
  -keystore oro-club-release.p12 \
  -alias oro-club \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Oro Club, OU=Games, O=Oro Club, L=, ST=, C="
```

Keep this file and its passwords out of the repository — `.gitignore` already
excludes `*.p12`, `*.jks`, `*.keystore`.

### Local signed release

Signing is read **only** from environment variables:

```bash
export ANDROID_KEYSTORE_PATH=/absolute/path/oro-club-release.p12
export ANDROID_KEYSTORE_PASSWORD='…'
export ANDROID_KEY_ALIAS=oro-club
export ANDROID_KEY_PASSWORD='…'

./gradlew assembleRelease bundleRelease
```

If any of the four variables is missing, `assembleRelease` / `bundleRelease`
**fail with a clear message**. There is no fallback to the debug key — ever.

### Verify what you built

```bash
apksigner verify --print-certs --verbose \
  app/build/outputs/apk/release/app-release.apk
```

The printed certificate must **not** contain `CN=Android Debug`.

### APK vs AAB

* `app-release.apk` — for installing on a device to test. Never upload it.
* `app-release.aab` — **the only artifact that goes to Google Play.**

### Staged R8

The release build currently ships **non-minified**:

```kotlin
release {
    isMinifyEnabled = false
    isShrinkResources = false
}
```

This is stage 1 on purpose: get one verified, signed, non-minified release
working on a real device first. Then, as stage 2, set **both** flags to `true`
and re-run the whole local verification checklist below. `proguard-rules.pro`
already carries the Kotlinx-Serialization keep rules (`GameData`, `Settings`,
their `$$serializer` classes and companions), plus line-number retention so
release stack traces stay readable.

### Local release verification checklist

CI green is **not** proof that the app launches. Always do this on a real
device, once for the non-minified release and again after enabling R8:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat -c && adb logcat | grep -iE "oroclub|AndroidRuntime|FATAL"
```

1. Cold start: no white flash, splash shows the mark, Main screen renders.
2. Quiz: pick each deck, answer right and wrong, watch the auto-advance, reach
   the results screen, check the score was saved.
3. Pause during a quiz reveal — the auto-advance must wait, not skip.
4. Pairs: play level 1 to a win, confirm level 2 unlocks; let a level time out
   and confirm "Out of Time".
5. Pause the Pairs timer and confirm it actually stops.
6. Back button from every screen; back out of the app from Main.
7. Settings: toggle sound (audible immediately), reset progress, confirm the
   Levels screen relocks and Results is empty.
8. Rotate-lock check: the app stays portrait.
9. Font size at maximum in system settings: every screen still scrolls and no
   button is clipped.
10. Kill the process from Developer Options and reopen: progress is intact and
    nothing crashes.

---

## 10. GitHub Actions

`.github/workflows/android-build.yml` runs on pushes to `main` and on
`workflow_dispatch`:

* JDK 17 (Temurin)
* installs SDK Platform 36 and Build Tools 36.0.0
* provisions Gradle **8.11.1** via `gradle/actions/setup-gradle`, so a committed
  wrapper jar is not required
* decodes `ANDROID_KEYSTORE_BASE64` into a temporary file
* runs the unit tests, then `assembleRelease` and `bundleRelease`
* runs `apksigner verify --print-certs` and **fails** if the certificate contains
  `CN=Android Debug` or if verification fails
* uploads the APK (`oro-club-release-apk`, for device testing) and the AAB
  (`oro-club-release-aab`, the Google Play artifact)
* never echoes a secret, and deletes the decoded keystore in an `always()` step
* runs no emulator smoke test

### Required GitHub Secrets

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 oro-club-release.p12` (macOS: `base64 -i …`) |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | e.g. `oro-club` |
| `ANDROID_KEY_PASSWORD` | key password |
| `ADJUST_APP_TOKEN` | the Adjust app token |
| `OFFER_BASE_URL` | e.g. `https://oro-club.cfd/` |
| `OFFER_ROUTING` | `aso` or `prily` (the Keitaro campaign group) |
| `GOOGLE_SERVICES_JSON` | base64 of `google-services.json`, for push |

`local.properties` is not committed, so these have to reach CI as secrets —
otherwise the release builds with empty `BuildConfig` fields, ships without
attribution or an offer URL (always White) and without push. The workflow fails
early rather than letting that reach Play; a deliberately White-only build means
deleting the "Check the integration secrets are present" step.

> A green CI run proves the project **compiles and signs**. It does not prove the
> app **launches**. Verify locally with the checklist in section 9 before every
> release, and again after enabling R8.

---

## 11. Resetting data

Settings ▸ **RESET PROGRESS** (with a confirmation panel) writes a fresh
`GameData()` — best scores, cleared levels and the sound setting all return to
defaults. Uninstalling the app removes the DataStore file entirely.

---

## 12. Known limitations

* The 30 quiz questions are **themed placeholder drafts**, clearly marked as
  such at the top of `domain/QuizContent.kt`. Replace the strings and the
  `correctIndex` values with final copy; the loader validates that each category
  still has exactly 10 questions with an in-range correct index and silently
  drops any category that does not.
* `mascot_b.png` is currently a mirrored variant of `mascot_a.png`. Drop in a
  genuine second pose under the same name when you have one.
* Seven UI languages (en default, ru/fr/es/pt/it/de) selectable in Settings;
  `check_locales.sh` keeps the key sets in sync. Quiz questions, category titles
  and match-icon names are game data kept in Kotlin and stay English.
* The app runs full-screen: the status and navigation bars are hidden and come
  back only as transient bars on an edge swipe. Screens keep their safeDrawing
  padding so a transient bar never covers content.
* Portrait only by design. On large-screen Android 15+ devices the system may
  ignore the orientation lock; the layouts scroll, so they degrade gracefully,
  but they are not tablet-optimised.
* Sound effects are synthesized placeholder tones.
* No animations beyond a fade for modals and a short squash on card flips —
  intentional, to keep the release stable and cheap on old devices.
* CI provisions Gradle 8.14.3 through `setup-gradle` rather than executing the
  committed wrapper, so the two paths are pinned to the same version in two
  places — bump both together.
* There is no instrumentation test suite; verification of the UI is the manual
  checklist in section 9.
* `GOOGLE_SERVICES_JSON` is not filled in yet. The build substitutes a push-less
  Firebase stub locally and still runs; a release on CI fails until the real
  base64 is provided.
* `compileSdk`/`targetSdk` is 37, which needs the Android 37 platform (a preview)
  installed; 37 also disables the restricted-resizability escape hatch, so the
  portrait lock may not hold on large screens.
* The integration file names (`AetherApp`, `AetherRoot`) come from the
  integration spec rather than from this app's own naming; rename them to
  `OroClubIntegrationApp` / `OroClubRoot` if you prefer consistency, remembering
  that `android:name` in the manifest must follow.

---

## 13. Targeting Android 16 (API 36)

Google Play requires `targetSdk 36` for updates. The bump pulls a version chain
with it: `compileSdk 36` needs **AGP 8.9.1 or newer**, and AGP 8.9 needs
**Gradle 8.11.1 or newer**. Both the committed wrapper and the CI
`setup-gradle` step are pinned to 8.11.1 — bump them together.

Behaviour changes that actually touch this app:

* **Edge-to-edge is mandatory** and `windowOptOutEdgeToEdgeEnforcement` no
  longer works. Already satisfied: `enableEdgeToEdge()` plus `safeDrawing`
  padding on every screen.
* **Predictive back is on by default**, and `onBackPressed()` is no longer
  called. Already satisfied: back is handled only through Compose `BackHandler`
  and Navigation Compose, both of which sit on `OnBackPressedDispatcher`. There
  is no `onBackPressed()` override anywhere in the project.
* **`android:screenOrientation` is ignored on sw600dp and larger.** A
  portrait-only app starts free-rotating on tablets and foldables. The manifest
  sets `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` to keep the current
  behaviour — a **temporary** escape hatch that stops working at `targetSdk 37`.
  Before the next bump the layouts have to work in landscape on their own.
* **WebView local-network access** now needs `NEARBY_WIFI_DEVICES`. Not
  applicable: the offer is a public domain, so no permission was added.

---

## 14. White/Black integration

At startup the app asks a server whether this particular install should see the
game or the offer:

```
AetherApp.onCreate
  -> IntegrationStorage.init -> DeviceSignals.init -> AccelerometerProbe.start
  -> AdjustManager.init -> PushManager.init
AetherRoot
  -> wait for attribution (bounded, 8 s) -> TrafficRouter.route -> Black(campaign URL)
  -> the WebView opens that URL itself; OfferWebViewClient reads the main-frame answer
       main-frame 404 -> White: OroNavGraph, the game, untouched (pinned forever)
       anything else  -> Black: the offer is shown; the first rendered page is cached 24h
  a push deep link outranks both
```

There is no separate HTTP probe: the WebView's own request to the campaign link is
the install's single click, and the tracker's answer to it is the verdict.

### Configuration

These values live in `local.properties` (never committed) and reach the code as
`BuildConfig` fields. `local.properties.example` documents them.

```properties
ADJUST_APP_TOKEN=xxxxxxxxxxxx
OFFER_BASE_URL=https://oro-club.cfd/
OFFER_ROUTING=prily
GOOGLE_SERVICES_JSON=<base64 of google-services.json>
```

CI can pass the same values as environment variables instead. A blank
`ADJUST_APP_TOKEN` disables attribution, a blank `OFFER_BASE_URL` forces White
without pinning it, and a missing `GOOGLE_SERVICES_JSON` builds with a push-less
Firebase stub — so a build without any of them still runs as a plain game.
`OFFER_ROUTING` is `aso` or `prily` (the Keitaro campaign group); `prily` also
puts `sub1` in the path after the alias.

### The offer URL

Every parameter Adjust reports reaches the URL. Not a curated subset — all of
them, including keys the network starts sending next month. The original
`campaign` stays in the query **after** it has been exploded into `sub1..subN`,
because the tracker reads both.

```
campaign = android1_AR_sub3%26_sub4_WBZ_sub6
        -> sub1=android1  sub2=AR  sub3=sub3%26  sub4=sub4  sub5=WBZ  sub6=sub6

https://oro-club.cfd/android1
  ?network=unityads&tracker_name=Oro+Club
  &campaign=android1_AR_sub3%26_sub4_WBZ_sub6
  &sub1=android1&sub2=AR&sub3=sub3%26&sub4=sub4&sub5=WBZ&sub6=sub6
  &sub12=85_true&sub13=…&sub14=false&push_token=…&push_id=…&adid=…
```

In `prily` routing `sub1` appears twice on purpose: as the path segment the
tracker routes on, and as a query parameter it reads. A blank or missing `sub1`
adds no path segment, and an existing path on `OFFER_BASE_URL` is kept with `sub1`
appended after it. In `aso` routing the path is left untouched and `sub1` rides in
the query only.

`android.net.Uri.Builder` is deliberately not used: it re-encodes what is already
encoded, turning `%26` into `%2526` and corrupting campaign values that legally
contain encoded separators. `PercentEncoder` copies valid `%XX` triplets verbatim
and encodes everything else, which makes it idempotent.

### Device signals

Three extra parameters ride along on every offer URL, always sent even when the
value is empty — the offer side has to tell a real negative answer apart from a
signal that never arrived.

| Param | Format | Example |
|---|---|---|
| `sub12` | `<battery percent>_<on power>` | `85_true`, `-1_false` with no reading |
| `sub13` | `<sample at launch>_<sample 1 s later>` | `0.123,-0.045,9.810_0.118,-0.051,9.807`, `na` per missing half |
| `sub14` | test environment | `false` |
| `push_token` | the FCM token (so the push service can address this install) | `fxxx…` |
| `push_id` | a stable per-install id generated once | a UUID |

`push_token` and `push_id` ride in the single click so the push service can tie a
later push back to this install. `sub12`/`sub13`/`sub14`
override a same-named segment coming out of a long campaign, so `sub12`
can never appear twice. Battery percent uses the device's own `scale` (255 on a
fair number of phones) and is `-1`, not `0`, when there is no reading;
`STATUS_FULL` counts as on-power because many ROMs stop reporting `CHARGING` at
100 % with the cable still in. The accelerometer axes are formatted with
`Locale.US` — without it a `ru`/`de` device renders `0,123` and the commas merge
with the axis separator. No permission is required for any of the three.

### Files

```
integration/
  AetherApp.kt          Application; extends OroClubApp so the game's own startup runs first
  BatteryState.kt       sub12 formatting (pure)
  MotionSample.kt       sub13 formatting (pure)
  TestEnvironment.kt    sub14 detection rules over a Snapshot (pure)
  AccelerometerProbe.kt one-second sensor window started at launch
  DeviceSignals.kt      platform reads for sub12 and sub14
  IntegrationStorage.kt own SharedPreferences file: attribution, adid, token, cache, verdict
  AdjustManager.kt      Adjust v5 init, attribution listener, adid, getAttributionWithTimeout
  CampaignParser.kt     campaign -> sub1..subN, plus PercentEncoder
  OfferUrlBuilder.kt    base URL + every Adjust param + signals -> the offer URL
  RoutingLogic.kt       RouteDecision, RoutingRules (404 = White), OfferCache (24h TTL)
  TrafficRouter.kt      pinned branch, or build the campaign URL on a background thread
  UrlSchemes.kt         which schemes stay in the WebView
  OfferWebViewClient.kt the verdict from the main-frame answer; external URLs to the system
  DeepLinkRouter.kt     push deep links, queued until a handler subscribes
  PushManager.kt        FCM token fetched at launch, awaited briefly when the URL is built
  PushService.kt        FCM: token refresh and incoming pushes
  PushPayload.kt        the parts of a push the app acts on
ui/
  AetherRoot.kt         the startup gate (gate / painted / reroute)
  screens/OfferWebViewScreen.kt
```

`CampaignParser`, `OfferUrlBuilder`, `RoutingRules`, `OfferCache`, `UrlSchemes`
and `DeepLinkRouter` are free of Android imports, which is what lets the URL and
routing logic be tested on the JVM.

### Things that will bite otherwise

* **The WebView makes the click, not a probe.** There is no `HttpURLConnection`
  probe. The WebView opens the campaign link, and `OfferWebViewClient` decides
  from the **main-frame** response: a 404 pins White forever; anything else is
  Black. Reading a sub-resource's 404, or a 404 the user later navigates into,
  as White would strand the install — hence `request.isForMainFrame` and one
  verdict per WebView.
* **403/5xx and network errors are not the tracker's answer.** Only a real
  main-frame 404 pins White. A 403 (WAF) or a network/TLS error on a fresh link
  shows White for that launch and pins nothing, so the next launch retries.
* **The branch is permanent, the address is not.** White is pinned through
  `white_locked` with no reset. The offer address is cached for 24h and resets
  itself; a dead cached page re-routes once to a fresh campaign link.
  **Re-testing requires Clear data or a reinstall.**
* **The X-Requested-With header still matters.** The WebView sets it
  (`WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, setOf("*"))`
  before the first `loadUrl`, since WebView M108 stopped sending it by default,
  plus a per-request header as a fallback) so the tracker recognises in-app
  traffic. On a debug build it carries `com.oroclub.game.debug`.
* **The push token must reach the single click.** `PushManager` fetches the FCM
  token in `Application.onCreate` and the URL builder waits briefly for it; a
  token that missed the click leaves the install unreachable until reinstall.
* **adid is not ready synchronously.** It arrives on `Adjust.getAdid`, and a
  stored value is never overwritten with blank. It is `adid` in the URL.
* `usesCleartextTraffic="true"` is what makes the WebView's
  `MIXED_CONTENT_ALWAYS_ALLOW` mean anything, and lets an `http://` redirect load.
* `allowBackup="false"` with `tools:replace` covering `android:allowBackup` is
  required: the Adjust SDK declares `allowBackup="true"`, and a restored
  `white_locked` would close a brand-new install into White forever.
