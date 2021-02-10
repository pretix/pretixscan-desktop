package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.setup.SetupBadRequestException
import eu.pretix.libpretixsync.setup.SetupBadResponseException
import eu.pretix.libpretixsync.setup.SetupManager
import eu.pretix.libpretixsync.setup.SetupServerErrorException
import eu.pretix.pretixscan.desktop.OkHttpClientFactory
import eu.pretix.pretixscan.desktop.PretixScanMain
import eu.pretix.pretixscan.desktop.VERSION
import eu.pretix.pretixscan.desktop.VERSION_CODE
import java.io.IOException
import javax.net.ssl.SSLException

enum class SetupResultState {
    ERR_SSL,
    ERR_IO,
    ERR_SERVERERROR,
    ERR_BADREQUEST,
    ERR_BADRESPONSE,
    OK
}

class SetupResult(val state: SetupResultState, val message: String = "")

class SetupController : BaseController() {
    fun configure(url: String, token: String): SetupResult {
        val setupm = SetupManager(
                System.getProperty("os.name"), System.getProperty("os.version"),
                "pretixSCAN", VERSION,
                OkHttpClientFactory()
        )
        try {
            val init = setupm.initialize(url, token)
            configStore.setDeviceConfig(init.url, init.api_token, init.organizer, init.device_id, init.unique_serial, VERSION_CODE)
            configStore.proxyMode = token.startsWith("proxy=")
            if (init.security_profile == "pretixscan_online_kiosk") {
                configStore.syncOrders = false
            }
            (app as PretixScanMain).reloadCheckProvider()
            return SetupResult(SetupResultState.OK, "")
        } catch (e: SSLException) {
            return SetupResult(SetupResultState.ERR_SSL, "")
        } catch (e: IOException) {
            return SetupResult(SetupResultState.ERR_IO, "")
        } catch (e: SetupServerErrorException) {
            return SetupResult(SetupResultState.ERR_SERVERERROR, "")
        } catch (e: SetupBadRequestException) {
            return SetupResult(SetupResultState.ERR_BADREQUEST, e.message ?: "")
        } catch (e: SetupBadResponseException) {
            return SetupResult(SetupResultState.ERR_BADRESPONSE, "")
        }
    }
}