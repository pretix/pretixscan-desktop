package eu.pretix.pretixscan.desktop.ui

import eu.pretix.libpretixsync.db.CheckInList
import eu.pretix.pretixscan.desktop.PretixScanMain

class SelectCheckInListController : BaseController() {

    fun getAllLists(): List<CheckInList> {
        var lists = (app as PretixScanMain).data().select(CheckInList::class.java)
                .where(CheckInList.EVENT_SLUG.eq(configStore.eventSlug))
        if (configStore.subEventId != null && configStore.subEventId!! > 0) {
            lists = lists.and(CheckInList.SUBEVENT_ID.eq(configStore.subEventId))
        }
        return lists.get().toList();
    }

    fun setList(list: CheckInList) {
        configStore.checkInListId = list.server_id
        configStore.checkInListName = list.name
        (app as PretixScanMain).reloadCheckProvider()
    }
}