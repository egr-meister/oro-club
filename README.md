# Oro Club — Golden Joker Club

A casual, single-player, **fully offline** Android game: a joker-themed quiz and
a nine-level Matching Pairs memory game, built with Kotlin and Jetpack Compose.

* Application ID: `com.oroclub.game`
* `minSdk 24` · `compileSdk 35` · `targetSdk 35` · JDK 17 · portrait only
* No internet permission, no account, no ads, no analytics, no payments,
  no backend, no cloud.

> **Not gambling.** Oro Club is a for-fun quiz and memory game with a joker /
> playing-card / luck *theme*. There is no wagering, no real-money play, no
> prizes and no simulated betting of any kind. Nothing in the app claims
> otherwise.

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

## 2. Offline by construction

`AndroidManifest.xml` declares **no permissions at all** — in particular no
`android.permission.INTERNET`. There are no services, no `WorkManager`, no
alarms, no notifications, no background work of any kind, and no keep-awake
flags. The app cannot reach the network even if it wanted to.

### Missing integrations (deliberate)

No networking library (Retrofit / OkHttp / Ktor), no Firebase, no ads SDK, no
analytics or crash reporting SDK, no payment or billing library, no maps, no
push, no login, no DI framework, no Room, no WebView, no native libraries.

### Privacy

The app collects nothing and sends nothing. The only data that exists is what
you produce by playing, and it lives in this app's private storage on this
device. Uninstalling the app removes it.

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
AGP 8.7.3 · Gradle 8.9 · JDK 17.

Every package the code imports is a **direct** catalog dependency, including
`androidx.compose.foundation`, `androidx.compose.animation` and
`androidx.annotation`.

---

## 6. Architecture

Simple MVVM, no DI framework.

```
OroClubApp (Application)
  └─ GameRepository            created once, backed by DataStore Preferences
       └─ GameSerializer       context-free JSON encode/decode (unit-tested)

ui/vm
  ProgressViewModel   ProgressUiState  (menu, levels, results, settings)
  QuizViewModel       QuizUiState
  PairsViewModel      PairsUiState
        ↑ all expose immutable state via StateFlow
        ↑ screens collect with collectAsStateWithLifecycle

domain
  GameLogic     levels, deck building, unlock/completion — context-free, tested
  QuizContent   the 3 × 10 questions — plain data, replaceable

ui/nav/OroNavGraph   Navigation Compose graph with typed arguments
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
3. The Gradle wrapper is committed and pinned to **Gradle 8.9**, so `./gradlew`
   works out of the box. If you ever need to regenerate it:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
4. Make sure the project JDK is **17** (`Settings ▸ Build Tools ▸ Gradle ▸
   Gradle JDK`) and that **SDK Platform 35** and **Build Tools 35.0.0** are
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

### 16 KB memory page size

The app is pure Kotlin/Compose with **no native libraries**, so there is nothing
to re-align — it is 16 KB page-size compatible by construction. Verify it anyway
on the artifact you are about to ship:

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
* installs SDK Platform 35 and Build Tools 35.0.0
* provisions Gradle **8.9** via `gradle/actions/setup-gradle`, so a committed
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
* English only. All copy is inline in the composables rather than in
  `strings.xml`, which keeps the code readable but means adding a language is a
  refactor, not a translation drop-in.
* Portrait only by design. On large-screen Android 15+ devices the system may
  ignore the orientation lock; the layouts scroll, so they degrade gracefully,
  but they are not tablet-optimised.
* Sound effects are synthesized placeholder tones.
* No animations beyond a fade for modals and a short squash on card flips —
  intentional, to keep the release stable and cheap on old devices.
* CI provisions Gradle 8.9 through `setup-gradle` rather than executing the
  committed wrapper, so the two paths are pinned to the same version in two
  places — bump both together.
* There is no instrumentation test suite; verification of the UI is the manual
  checklist in section 9.
