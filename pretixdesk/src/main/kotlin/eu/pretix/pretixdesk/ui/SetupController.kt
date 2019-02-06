package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.setup.SetupBadRequestException
import eu.pretix.libpretixsync.setup.SetupBadResponseException
import eu.pretix.libpretixsync.setup.SetupManager
import eu.pretix.libpretixsync.setup.SetupServerErrorException
import eu.pretix.pretixdesk.OkHttpClientFactory
import eu.pretix.pretixdesk.PretixDeskMain
import eu.pretix.pretixdesk.VERSION
import eu.pretix.pretixdesk.VERSION_CODE
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
            (app as PretixDeskMain).reloadCheckProvider()
            return SetupResult(SetupResultState.OK, "")
        } catch (e: SSLException) {
            return SetupResult(SetupResultState.ERR_SSL, "")
        } catch (e: IOException) {
            return SetupResult(SetupResultState.ERR_IO, "")
        } catch (e: SetupServerErrorException) {
            return SetupResult(SetupResultState.ERR_SERVERERROR, "")
        } catch (e: SetupBadRequestException) {
            return SetupResult(SetupResultState.ERR_BADREQUEST, "")
        } catch (e: SetupBadResponseException) {
            return SetupResult(SetupResultState.ERR_BADRESPONSE, "")
        }
    }
}