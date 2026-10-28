package com.oroclub.game.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * App language, driven by AppCompatDelegate (the single source of truth, so no copy is
 * kept in DataStore). Survives a restart below API 33 only with all three conditions in
 * settings-and-locales.md: AppCompatActivity, the AppLocalesMetadataHolderService in the
 * manifest, and `locale` kept out of the Activity's configChanges.
 */
object LocaleManager {

    /**
     * Language tag to the name written in that language itself, so someone who switched by
     * accident can find the way back. The empty tag ("follow system") is offered separately
     * with a localised label.
     */
    val LANGUAGES: List<Pair<String, String>> = listOf(
        "en" to "English",
        "ru" to "Русский",
        "fr" to "Français",
        "es" to "Español",
        "pt" to "Português",
        "it" to "Italiano",
        "de" to "Deutsch"
    )

    /** null or blank means "follow the system" - an empty locale list, not a fixed tag. */
    fun applyLocale(tag: String?) {
        val locales =
            if (tag.isNullOrBlank()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** The chosen tag, or null for "system". */
    fun currentTag(): String? =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().takeIf { it.isNotBlank() }

    /** The chosen language's base code (e.g. "en" from "en-US"), or null for "system". */
    fun currentLanguage(): String? =
        currentTag()?.substringBefore('-')?.lowercase()
}
