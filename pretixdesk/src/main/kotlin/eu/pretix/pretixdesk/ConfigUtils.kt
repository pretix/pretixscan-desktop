package eu.pretix.pretixdesk

import java.net.URLDecoder
import java.util.HashMap


fun queryToMap(query: String): Map<String, String> {
    val params = query.split("&".toRegex())
    val map = HashMap<String, String>()
    for (param in params) {
        val parts = param.split("=".toRegex(), 2)
        val name = parts[0]
        val value = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
        map[name] = value
    }
    return map
}