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
    policy: BadgePrintPolicy,
    resultState: ResultState,
    position: JSONObject?
): Boolean {
    return when (policy) {
        BadgePrintPolicy.WHEN_BUTTON_PRESSED -> false
        BadgePrintPolicy.ALWAYS -> resultState == ResultState.SUCCESS && position != null
        BadgePrintPolicy.ONCE_IF_NOT_PRINTED ->
            resultState == ResultState.SUCCESS && position != null && !isPreviouslyPrinted(position)
    }
}
