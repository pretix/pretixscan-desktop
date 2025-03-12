package app

import eu.pretix.libpretixsync.SentryInterface


class DesktopSentryImpl : SentryInterface {
    override fun addHttpBreadcrumb(url: String?, method: String?, statusCode: Int) {
        // do nothing
    }

    override fun addBreadcrumb(a: String?, b: String?) {
        // do nothing
    }

    override fun captureException(t: Throwable?) {
        // do nothing
    }

    override fun captureException(t: Throwable?, message: String?) {
        // do nothing
    }
}