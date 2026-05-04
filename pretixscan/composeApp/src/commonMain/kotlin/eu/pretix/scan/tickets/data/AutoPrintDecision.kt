package eu.pretix.scan.tickets.data

import org.json.JSONObject

fun isPreviouslyPrinted(position: JSONObject): Boolean {
    val printLogs = position.optJSONArray("print_logs") ?: return false
    for (i in 0 until printLogs.length()) {
        val log = printLogs.optJSONObject(i) ?: continue
        if (log.optBoolean("successful", false) && log.optString("type") == "badge") {
            return true
        }
    }
    return false
}

fun shouldAutoPrint(
    autoPrintBadges: Boolean,
    resultState: ResultState,
    position: JSONObject?
): Boolean {
    if (!autoPrintBadges) return false
    if (resultState != ResultState.SUCCESS) return false
    if (position == null) return false
    return !isPreviouslyPrinted(position)
}
