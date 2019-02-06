package eu.pretix.pretixdesk

import eu.pretix.libpretixsync.api.HttpClientFactory
import okhttp3.OkHttpClient

class OkHttpClientFactory : HttpClientFactory {
    override fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .build()
    }
}
