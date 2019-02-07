package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.setup.EventManager
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.pretixdesk.PretixDeskMain

class SelectEventController : BaseController() {
    fun fetchEvents(): List<RemoteEvent> {
        val em = EventManager(
                (app as PretixDeskMain).data(),
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
    }
}