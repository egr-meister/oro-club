package com.oroclub.game.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.oroclub.game.BuildConfig
import com.oroclub.game.integration.OfferWebViewClient

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OfferWebViewScreen(url: String) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Holds the live WebView so back navigation can consult its history.
    val webViewRef = remember { arrayOfNulls<WebView>(1) }
    // Load-once flag keyed on the URL. Comparing against webView.url instead would drag
    // the user back to the landing page on every recomposition after they navigate.
    val loaded = remember(url) { booleanArrayOf(false) }

    BackHandler {
        val webView = webViewRef[0]
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            // Without this the user is trapped: there is no White UI behind the offer
            // to fall back to.
            activity?.finish()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    javaScriptCanOpenWindowsAutomatically = true
                    // Offer pages routinely pull assets over http from an https shell.
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                // Must happen before the first loadUrl, otherwise that request - the one
                // that matters - goes out without the header.
                allowRequestedWithHeader(settings)
                webChromeClient = WebChromeClient()
                webViewClient = OfferWebViewClient(ctx)
                webViewRef[0] = this
            }
        },
        update = { webView ->
            webViewRef[0] = webView
            if (!loaded[0]) {
                loaded[0] = true
                // Belt and braces for the main frame, in case the WebView build rejected
                // the allow-list above. Extra headers apply to this request only: they do
                // not survive a redirect and do not reach sub-resources.
                webView.loadUrl(url, mapOf(REQUESTED_WITH to BuildConfig.APPLICATION_ID))
            }
        },
        onRelease = { webView ->
            // Offer pages are ad-heavy: without this the Activity leaks on every
            // configuration change and JS timers and media keep running.
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
            webViewRef[0] = null
        }
    )
}

private const val REQUESTED_WITH = "X-Requested-With"

/**
 * Restores the `X-Requested-With` header the WebView stopped sending by default in M108.
 *
 * The tracker identifies in-app traffic by that header, so without it the bundle id never
 * appears in its stats. `"*"` is the documented wildcard rule and covers every origin in a
 * redirect chain as well as sub-requests (XHR, iframes) - per-origin rules added later
 * from onPageStarted would be too late for the redirect that already went out.
 *
 * Both failure modes are caught: the API throws IllegalArgumentException on a rule the
 * WebView build will not accept, and UnsupportedOperationException when the feature is
 * missing. It is documented as a transitional API and may be removed, which is why the
 * per-request header on loadUrl stays as a fallback.
 */
private fun allowRequestedWithHeader(settings: WebSettings) {
    if (!WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
        Log.w("OfferWebView", "REQUESTED_WITH_HEADER_ALLOW_LIST unsupported on this WebView")
        return
    }
    runCatching {
        WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, setOf("*"))
    }.onFailure {
        Log.w("OfferWebView", "Could not set the X-Requested-With allow list", it)
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
