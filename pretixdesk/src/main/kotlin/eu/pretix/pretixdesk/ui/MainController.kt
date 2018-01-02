package eu.pretix.pretixdesk.ui

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import eu.pretix.libpretixsync.DummySentryImplementation
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.QueuedCheckIn
import eu.pretix.libpretixsync.sync.SyncManager
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.VERSION
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.json.JSONObject
import tornadofx.Controller
import tornadofx.get
import tornadofx.toJSON
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*


class MainController : BaseController() {

    fun reloadCheckProvider() {
        provider = (app as PretixDeskMain).newCheckProvider()
    }

    fun toggleAsync(value: Boolean) {
        configStore.asyncModeEnabled = value
        reloadCheckProvider()
    }

    fun handleSearchInput(value: String): List<TicketCheckProvider.SearchResult>? {
        return provider.search(value)
    }

    fun handleScanInput(value: String): TicketCheckProvider.CheckResult? {
        return provider.check(value)
    }

    fun updateCheck() {
        if ((System.currentTimeMillis() - configStore.lastUpdateCheck) > 24 * 3600 * 1000) {
                        val payload = JSONObject()
            payload.put("version", VERSION)
            val request = "https://pretix.eu/.update_check/pretixdesk/".httpPost().body(payload.toString())
            request.headers["Content-Type"] = "application/json"
            val (_, response, result) = request.responseString()
            if (response.statusCode == 200) {
                try {
                    val data = JSONObject(result.get())
                    if ("ok".equals(data.optString("status"))) {
                        if (data.getJSONObject("version").getBoolean("updatable")) {
                            configStore.lastUpdateCheck = System.currentTimeMillis()
                            configStore.updateCheckNewerVersion = data.getJSONObject("version").getString("latest")
                        } else {
                            configStore.lastUpdateCheck = System.currentTimeMillis()
                            configStore.updateCheckNewerVersion = ""
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateCheckNewerVersion(): String {
        return configStore.updateCheckNewerVersion;
    }
}