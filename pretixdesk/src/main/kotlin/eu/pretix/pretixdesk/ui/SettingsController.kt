package eu.pretix.pretixdesk.ui

import eu.pretix.libpretixsync.db.*
import eu.pretix.pretixdesk.PretixDeskMain

class SettingsController : BaseController() {

    fun resetApp() {
        configStore.resetEventConfig()
        (app as PretixDeskMain).data().delete(QueuedCheckIn::class.java).get()
        (app as PretixDeskMain).data().delete(BadgeLayout::class.java).get()
        (app as PretixDeskMain).data().delete(CheckInList_Item::class.java).get()
        (app as PretixDeskMain).data().delete(CheckInList::class.java).get()
        (app as PretixDeskMain).data().delete(Closing::class.java).get()
        (app as PretixDeskMain).data().delete(Event::class.java).get()
        (app as PretixDeskMain).data().delete(Question_Item::class.java).get()
        (app as PretixDeskMain).data().delete(Question_Item::class.java).get()
        (app as PretixDeskMain).data().delete(Item::class.java).get()
        (app as PretixDeskMain).data().delete(ItemCategory::class.java).get()
        (app as PretixDeskMain).data().delete(Order::class.java).get()
        (app as PretixDeskMain).data().delete(OrderPosition::class.java).get()
        (app as PretixDeskMain).data().delete(Question::class.java).get()
        (app as PretixDeskMain).data().delete(Quota::class.java).get()
        (app as PretixDeskMain).data().delete(Receipt::class.java).get()
        (app as PretixDeskMain).data().delete(ReceiptLine::class.java).get()
        (app as PretixDeskMain).data().delete(ResourceLastModified::class.java).get()
        (app as PretixDeskMain).data().delete(SubEvent::class.java).get()
        (app as PretixDeskMain).data().delete(TaxRule::class.java).get()
        (app as PretixDeskMain).data().delete(TicketLayout::class.java).get()
    }

    fun hasLocalChanges(): Boolean {
        return (app as PretixDeskMain).data().count(QueuedCheckIn::class.java).get().value() > 0
    }

    fun toggleSound(value: Boolean ) {
        configStore.playSound = value
    }
}