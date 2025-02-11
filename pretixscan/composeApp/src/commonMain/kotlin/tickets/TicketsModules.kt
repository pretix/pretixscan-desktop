package tickets

import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import org.koin.dsl.module
import org.koin.dsl.onClose
import tickets.data.ConnectivityHelper
import tickets.data.TicketCodeHandler
import tickets.presentation.QuestionsDialogViewModel
import tickets.presentation.TicketHandlingDialogViewModel
import tickets.presentation.TicketSearchBarViewModel


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