package com.oroclub.game.integration

/**
 * Single entry point for deep links. A push notification carrying a `url` hands it here;
 * the root composable subscribes and turns it into a WebView destination.
 */
object DeepLinkRouter {

    private val lock = Any()
    private var handler: ((String) -> Unit)? = null
    // Links can arrive from a cold start before any composable has subscribed.
    private val pending = ArrayDeque<String>()

    fun setHandler(newHandler: (String) -> Unit) {
        val drained: List<String>
        synchronized(lock) {
            handler = newHandler
            drained = pending.toList()
            pending.clear()
        }
        // Delivered outside the lock: the handler touches Compose state and may re-enter.
        drained.forEach(newHandler)
    }

    fun clearHandler() {
        synchronized(lock) { handler = null }
    }

    fun handle(url: String?) {
        if (url.isNullOrBlank()) return
        val current: ((String) -> Unit)?
        synchronized(lock) {
            current = handler
            // Queued only when nobody is listening, so each link is delivered exactly once.
            if (current == null) pending.addLast(url)
        }
        current?.invoke(url)
    }
}
