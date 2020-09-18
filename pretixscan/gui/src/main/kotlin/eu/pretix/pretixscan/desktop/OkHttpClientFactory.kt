package eu.pretix.pretixscan.desktop

import eu.pretix.libpretixsync.api.DefaultHttpClientFactory
import eu.pretix.libpretixsync.api.HttpClientFactory
import okhttp3.OkHttpClient
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class OkHttpClientFactory : DefaultHttpClientFactory() {
    override fun buildClient(ignore_ssl: Boolean): OkHttpClient {
        Security.addProvider(BouncyCastleProvider())
        return super.buildClient(ignore_ssl)
    }
}
