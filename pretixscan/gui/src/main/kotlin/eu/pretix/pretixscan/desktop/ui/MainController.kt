package eu.pretix.pretixscan.desktop.ui

import com.github.kittinunf.fuel.httpPost
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.VERSION
import io.requery.sql.StatementExecutionException
import org.json.JSONObject
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.List


class MainController : BaseController() {

    fun toggleAsync(value: Boolean) {
        configStore.asyncModeEnabled = value
        (app as PretixScanMain).reloadCheckProvider()
    }

    fun handleSearchInput(value: String): List<TicketCheckProvider.SearchResult>? {
        if (configStore.eventSlug == null || configStore.checkInListId == 0L) {
            // not yet set up
            return null
        }
        return withRetry {
            return@withRetry (app as PretixScanMain).provider.search(value, 1)
        }
    }

    private fun<T> withRetry(f: () -> T): T {
        var exc: java.lang.Exception? = null
        for (retryCount in 0 .. 10) {
            try {
                return f()
            } catch (e: StatementExecutionException) {
                if (e.cause?.message?.contains("SQLITE_BUSY") == true) {
                    exc = e
                    Thread.sleep(1000)
                } else {
                    throw e
                }
            }
        }
        if (exc != null) {
            throw exc
        }
        throw RuntimeException("Impossible retry state reached")
    }

    fun handleScanInput(value: String, answers: List<Answer>? = null, ignore_pending: Boolean=false, type: TicketCheckProvider.CheckInType): TicketCheckProvider.CheckResult? {
        return withRetry {
            if (answers != null) {
                return@withRetry (app as PretixScanMain).provider.check(value, answers, ignore_pending, true, type)
            } else {
                return@withRetry (app as PretixScanMain).provider.check(value, ArrayList<Answer>(), ignore_pending, true, type)
            }
        }
    }

    fun updateCheck() {
        if ((System.currentTimeMillis() - configStore.lastUpdateCheck) > 24 * 3600 * 1000) {
                        val payload = JSONObject()
            payload.put("version", VERSION)
            val request = "https://pretix.eu/.update_check/pretixscan-desktop/".httpPost().body(payload.toString())
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
