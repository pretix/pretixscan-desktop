package eu.pretix.scan.tickets

import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.scan.tickets.data.ConnectivityHelper
import eu.pretix.scan.tickets.data.TicketCodeHandler
import eu.pretix.scan.tickets.presentation.QuestionsDialogViewModel
import eu.pretix.scan.tickets.presentation.TicketHandlingDialogViewModel
import eu.pretix.scan.tickets.presentation.TicketSearchBarViewModel
import org.koin.dsl.module
import org.koin.dsl.onClose


val ticketsModule = module {
    factory<TicketCodeHandler> {
        TicketCodeHandler(get(), get(), get(), get(), get(), get(), get())
    }
    factory<ConnectivityHelper> {
        ConnectivityHelper()
    }
    factory {
        TicketSearchBarViewModel(get(), get())
    }
    factory {
        TicketHandlingDialogViewModel(get(), get())
    }
    factory {
        QuestionsDialogViewModel(get())
    }
    factory<GadulkaPlayer> {
        GadulkaPlayer()
    } onClose {
        it?.release()
    }
}