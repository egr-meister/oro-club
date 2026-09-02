package com.oroclub.game.integration

/**
 * Assembles the offer URL from the base URL and everything AppsFlyer reported.
 *
 * Built by string concatenation on purpose: android.net.Uri.Builder percent-encodes
 * values that are already encoded, so a campaign carrying `%26` would reach the tracker
 * as `%2526`.
 *
 * Shape of the result:
 *
 *     https://domain.com/{sub1}?{every AppsFlyer param}&campaign={campaign}&sub1=..&subN=..&appsflyer_id=..
 *
 * `sub1` appears twice on purpose - once as a path segment the tracker routes on, once as
 * a query parameter it reads. Neither replaces the other.
 */
object OfferUrlBuilder {

    private const val CAMPAIGN_KEY = "campaign"
    private const val APPSFLYER_ID_KEY = "appsflyer_id"
    private const val SUB1_KEY = "sub1"

    /**
     * @param extra parameters the app contributes itself (device signals). They are
     *   applied last so a campaign long enough to produce its own `sub12` cannot end up
     *   in the URL twice with two different values.
     */
    fun build(
        baseUrl: String,
        appsFlyerParams: Map<String, String>,
        appsFlyerId: String?,
        extra: Map<String, String> = emptyMap()
    ): String {
        val base = baseUrl.trim()
        if (base.isEmpty()) return ""

        // Split without decoding - anything already encoded must pass through as-is.
        val hashIndex = base.indexOf('#')
        val fragment = if (hashIndex >= 0) base.substring(hashIndex) else ""
        val withoutFragment = if (hashIndex >= 0) base.substring(0, hashIndex) else base

        val questionIndex = withoutFragment.indexOf('?')
        val path = if (questionIndex >= 0) withoutFragment.substring(0, questionIndex) else withoutFragment
        val baseQuery = if (questionIndex >= 0) withoutFragment.substring(questionIndex + 1) else ""

        val params = LinkedHashMap<String, String>()
        parseQuery(baseQuery, params)
        // Every key AppsFlyer sent, in arrival order. Filtering to a known subset here is
        // the single most common way this integration silently loses attribution.
        params.putAll(appsFlyerParams)

        val campaign = appsFlyerParams[CAMPAIGN_KEY].orEmpty()
        val subs = CampaignParser.parse(campaign)

        // The tracker reads the raw campaign as well as the exploded parts, so it stays.
        if (campaign.isNotEmpty()) params[CAMPAIGN_KEY] = campaign

        // Parsed subs win over any sub1..subN AppsFlyer happened to send itself.
        // Re-putting an existing key replaces the value in place, so ordering stays
        // deterministic and no query key is emitted twice.
        params.putAll(subs)

        // The app's own signals win over a same-named campaign segment: a campaign of 12
        // or more segments produces its own sub12, and which of two identical keys the
        // tracker reads is undefined. Re-putting replaces the value in place, so the
        // campaign's position in the query is preserved and no key is emitted twice.
        params.putAll(extra)

        // A blank id would reach the tracker as an empty appsflyer_id, which reads as
        // "attributed to nothing" rather than "not known yet".
        if (!appsFlyerId.isNullOrBlank()) params[APPSFLYER_ID_KEY] = appsFlyerId.trim()

        // sub1 stays in the query as well - appending it to the path does not consume it.
        val finalPath = appendSub1(path, params[SUB1_KEY].orEmpty())
        val query = CampaignParser.toQuery(params)

        return buildString {
            append(finalPath)
            if (query.isNotEmpty()) {
                append('?')
                append(query)
            }
            append(fragment)
        }
    }

    /**
     * Appends sub1 as an extra path segment after whatever path the base URL already has,
     * so `https://d.cfd/` + `android1` becomes `https://d.cfd/android1` and
     * `https://d.cfd/offer/` + `test` becomes `https://d.cfd/offer/test`.
     *
     * A blank sub1 adds nothing - an empty segment would produce a trailing slash the
     * tracker reads as a different route. The already-appended check keeps build() safe to
     * run twice over its own output, which happens whenever a cached URL is rebuilt.
     */
    private fun appendSub1(path: String, sub1: String): String {
        if (sub1.isBlank()) return path
        val segment = PercentEncoder.encodePathSegment(sub1)
        val trimmed = path.trimEnd('/')
        if (trimmed.endsWith("/$segment")) return path
        return "$trimmed/$segment"
    }

    private fun parseQuery(query: String, into: LinkedHashMap<String, String>) {
        if (query.isEmpty()) return
        for (pair in query.split('&')) {
            if (pair.isEmpty()) continue
            val eq = pair.indexOf('=')
            if (eq < 0) into[pair] = "" else into[pair.substring(0, eq)] = pair.substring(eq + 1)
        }
    }
}
