package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.queryToMap
import org.json.JSONException
import org.json.JSONObject
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
    private fun configureLikePretixdroid(jsonData: String): SetupResult {
        try {
            val jsonobject = JSONObject(jsonData)

            if (!jsonobject.has("version") || !jsonobject.has("url") || !jsonobject.has("key")) {
                return SetupResult.INVALID_URL
            }
            if (jsonobject.getInt("version") < 3) {
                return SetupResult.VERSION_PRETIX_OLD
            }
            if (jsonobject.getInt("version") > PretixApi.SUPPORTED_API_VERSION) {
                return SetupResult.VERSION_PRETIXDROID_OLD
            }

            configStore.resetEventConfig()

            configStore.setEventConfig(
                    jsonobject.getString("url"),
                    jsonobject.getString("key"),
                    jsonobject.getInt("version"),
                    jsonobject.optBoolean("show_info", true),
                    jsonobject.optBoolean("allow_search", true)
            )
            (app as PretixDeskMain).reloadCheckProvider()
            return SetupResult.OK

        } catch (e: JSONException) {
            e.printStackTrace()
            return SetupResult.INVALID_URL
        }
    }

    fun configure(rawUrl: String): SetupResult {
        if (rawUrl.startsWith("{")) {
            return configureLikePretixdroid(rawUrl);
        }
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
                    params.getOrDefault("show_info", "True") == "True",
                    params.getOrDefault("allow_search", "True") == "True"
            )
            (app as PretixDeskMain).reloadCheckProvider()
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