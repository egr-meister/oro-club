# ---------------------------------------------------------------------------
# Oro Club — R8 / ProGuard rules
# The release build currently ships NON-MINIFIED (stage 1). These rules are in
# place so that flipping isMinifyEnabled/isShrinkResources to true (stage 2)
# does not break Kotlinx Serialization or Compose.
# ---------------------------------------------------------------------------

# --- Kotlinx Serialization -------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.**

# Keep the generated serializers of every @Serializable class in the app.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.oroclub.game.data.**$$serializer { *; }
-keepclassmembers class com.oroclub.game.data.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.oroclub.game.data.GameData { *; }
-keep class com.oroclub.game.data.Settings { *; }

# Serialization runtime internals reflected on at load time.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Kotlin / Coroutines ---------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlin.Metadata { public *; }

# --- Compose ---------------------------------------------------------------
# Compose ships its own consumer rules; these only silence known-safe warnings.
-dontwarn androidx.compose.**

# --- Integration SDKs ------------------------------------------------------
-keep class com.appsflyer.** { *; }
-dontwarn com.appsflyer.**
-keep class com.android.installreferrer.** { *; }
-dontwarn com.android.installreferrer.**
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# --- App entry points ------------------------------------------------------
-keep class com.oroclub.game.OroClubApp { *; }
-keep class com.oroclub.game.integration.AetherApp { *; }
-keep class com.oroclub.game.MainActivity { *; }

# Keep line numbers so release crash reports stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
