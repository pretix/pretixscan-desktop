package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.db.QueuedCheckIn
import eu.pretix.libpretixsync.db.Ticket
import eu.pretix.pretixdesk.PretixDeskMain

class SettingsController : BaseController() {

    fun resetApp() {
        configStore.resetEventConfig()
        (app as PretixDeskMain).data().delete(QueuedCheckIn::class.java).get()
        (app as PretixDeskMain).data().delete(Ticket::class.java).get()
    }

    fun hasLocalChanges(): Boolean {
        return (app as PretixDeskMain).data().count(QueuedCheckIn::class.java).get().value() > 0
    }
}