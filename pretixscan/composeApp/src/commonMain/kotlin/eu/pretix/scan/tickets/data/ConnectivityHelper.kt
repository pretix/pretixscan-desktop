package eu.pretix.scan.tickets.data

import eu.pretix.libpretixsync.sync.SyncManager

class ConnectivityHelper : SyncManager.CheckConnectivityFeedback {
    override fun recordError() {
        // TODO: implement recording this error
    }

    override fun recordSuccess(durationInMillis: Long?) {
        // TODO: implement recording this success
    }

}

