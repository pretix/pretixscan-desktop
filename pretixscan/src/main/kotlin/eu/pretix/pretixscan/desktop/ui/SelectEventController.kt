package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.pretixscan.desktop.PretixScanMain

class SelectEventController : BaseController() {
    fun fetchEvents(): List<RemoteEvent> {
        val em = EventManager(
                (app as PretixScanMain).data(),
                PretixApi.fromConfig(configStore),
                configStore
        )
        return em.getAvailableEvents()
    }

    fun setEvent(event: RemoteEvent) {
        configStore.eventSlug = event.slug
        configStore.subEventId = event.subevent_id ?: 0L
        configStore.eventName = event.name
        configStore.checkInListId = 0
        (app as PretixScanMain).reloadCheckProvider()
    }
}