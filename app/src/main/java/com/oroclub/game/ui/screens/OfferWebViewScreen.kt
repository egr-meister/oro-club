package com.oroclub.game.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
                webChromeClient = WebChromeClient()
                webViewClient = OfferWebViewClient(ctx)
                webViewRef[0] = this
            }
        },
        update = { webView ->
            webViewRef[0] = webView
            if (!loaded[0]) {
                loaded[0] = true
                webView.loadUrl(url)
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

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
