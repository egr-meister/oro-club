import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Release signing is read exclusively from environment variables.
 * A release artifact is NEVER signed with the debug key: if the variables are
 * missing, the build fails with a clear message instead of falling back.
 */
/**
 * Integration keys live in local.properties (never committed) and reach the code as
 * BuildConfig fields. CI passes them as environment variables instead.
 */
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String, default: String = ""): String =
    System.getenv(name) ?: localProperties.getProperty(name) ?: default

// Names are deliberately env-prefixed: inside a signingConfig block the plain
// names would resolve to the config's own (still empty) properties.
val envKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val envKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val envKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val envKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")

val hasReleaseSigning: Boolean =
    !envKeystorePath.isNullOrBlank() &&
        !envKeystorePassword.isNullOrBlank() &&
        !envKeyAlias.isNullOrBlank() &&
        !envKeyPassword.isNullOrBlank()

android {
    namespace = "com.oroclub.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oroclub.game"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"

        vectorDrawables { useSupportLibrary = true }

        // buildConfigField values are pasted into generated Java verbatim, which is why
        // the escaped quotes are part of the string.
        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"${secret("APPSFLYER_DEV_KEY")}\"")
        buildConfigField("String", "OFFER_BASE_URL", "\"${secret("OFFER_BASE_URL")}\"")
        buildConfigField("String", "ONESIGNAL_APP_ID", "\"${secret("ONESIGNAL_APP_ID")}\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = File(envKeystorePath!!)
                storePassword = envKeystorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // STAGE 1 (current): ship a verified non-minified release first.
            // STAGE 2: after that build is verified on a device, flip BOTH flags
            // to true and re-run the full local release checklist in the README.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // Overrides android.defaults.buildfeatures.buildconfig=false in gradle.properties:
        // the integration reads its keys through BuildConfig.
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

/**
 * Hard guard: any attempt to produce a release APK/AAB without the four signing
 * environment variables fails the build. Debug signing of a release artifact is
 * impossible by construction.
 */
if (!hasReleaseSigning) {
    val failIfReleaseRequested = tasks.register("failOnUnsignedRelease") {
        doFirst {
            throw GradleException(
                buildString {
                    appendLine("Oro Club: release signing is not configured.")
                    appendLine("Set ALL of these environment variables before building a release artifact:")
                    appendLine("  ANDROID_KEYSTORE_PATH      (absolute path to the PKCS12 keystore)")
                    appendLine("  ANDROID_KEYSTORE_PASSWORD")
                    appendLine("  ANDROID_KEY_ALIAS")
                    appendLine("  ANDROID_KEY_PASSWORD")
                    appendLine("Release artifacts are never signed with the debug key.")
                }
            )
        }
    }
    tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }
        .configureEach { dependsOn(failIfReleaseRequested) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Every package imported by the app is a direct dependency.
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Integration layer: attribution and push.
    implementation(libs.appsflyer)
    implementation(libs.onesignal)
    // Re-enables the X-Requested-With header the WebView stopped sending in M108.
    implementation(libs.androidx.webkit)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
