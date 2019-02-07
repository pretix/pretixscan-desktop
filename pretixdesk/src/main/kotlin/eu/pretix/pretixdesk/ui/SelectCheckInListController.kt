package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.db.CheckInList
import eu.pretix.libpretixsync.setup.RemoteEvent
import eu.pretix.pretixdesk.PretixDeskMain

class SelectCheckInListController : BaseController() {

    fun getAllLists(): List<CheckInList> {
        var lists = (app as PretixDeskMain).data().select(CheckInList::class.java)
                .where(CheckInList.EVENT_SLUG.eq(configStore.eventSlug))
        if (configStore.subEventId != null && configStore.subEventId!! > 0) {
            lists = lists.and(CheckInList.SUBEVENT_ID.eq(configStore.subEventId))
        }
        return lists.get().toList();
    }

    fun setList(list: CheckInList) {
        configStore.checkInListId = list.id
    }
}