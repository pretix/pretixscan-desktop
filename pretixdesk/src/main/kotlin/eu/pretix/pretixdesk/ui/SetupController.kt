package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.queryToMap
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

enum class SetupResult {
    INVALID_URL,
    VERSION_PRETIXDROID_OLD,
    VERSION_PRETIX_OLD,
    OK
}

class SetupController : BaseController() {
    fun reloadCheckProvider() {
        provider = (app as PretixDeskMain).newCheckProvider()
    }

    fun configure(rawUrl: String): SetupResult {
        try {
            val url = URI(rawUrl)

            if (url.scheme != "pretixdesk" || url.host != "setup") {
                return SetupResult.INVALID_URL
            }
            val params = queryToMap(url.query)
            if (!params.containsKey("version") || !params.containsKey("url") || !params.containsKey("key")) {
                return SetupResult.INVALID_URL
            }
            if (Integer.parseInt(params.get("version")) < 3) {
                return SetupResult.VERSION_PRETIX_OLD
            }
            if (Integer.parseInt(params.get("version")) > PretixApi.SUPPORTED_API_VERSION) {
                return SetupResult.VERSION_PRETIXDROID_OLD
            }

            configStore.resetEventConfig()

            configStore.setEventConfig(
                    params.get("url")!!,
                    params.get("key")!!,
                    Integer.parseInt(params.get("version")),
                    params.getOrDefault("show_info", "true") == "true",
                    params.getOrDefault("allow_search", "true") == "true"
            )
            reloadCheckProvider()
            return SetupResult.OK
        } catch(e: MalformedURLException) {
            e.printStackTrace()
            return SetupResult.INVALID_URL
        } catch(e: NumberFormatException) {
            e.printStackTrace()
            return SetupResult.INVALID_URL
        }
    }
}