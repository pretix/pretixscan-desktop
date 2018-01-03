package eu.pretix.pretixdesk

import it.sauronsoftware.junique.JUnique
import it.sauronsoftware.junique.MessageHandler
import tornadofx.App
import tornadofx.FXEvent
import java.net.URLDecoder
import java.util.HashMap

class ConfigureEvent(val rawUrl: String) : FXEvent()

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
fun App.acquireLock(appId: String, f: (message: String) -> String) {
    JUnique.acquireLock(appId, object : MessageHandler {
        public override fun handle(message: String) : String {
            return f(message)
        }
    })
}