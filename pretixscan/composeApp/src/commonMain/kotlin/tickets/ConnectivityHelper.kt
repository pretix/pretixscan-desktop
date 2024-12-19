package tickets

import eu.pretix.libpretixsync.sync.SyncManager

class ConnectivityHelper: SyncManager.CheckConnectivityFeedback {
    override fun recordError() {
        println("TODO: implement recording this error.")
    }

    override fun recordSuccess(durationInMillis: Long?) {
        println("TODO: implement recording this success.")
    }

}

