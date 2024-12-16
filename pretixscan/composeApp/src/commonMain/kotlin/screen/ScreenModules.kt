package screen

import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import org.koin.dsl.module
import org.koin.dsl.onClose
import screen.main.mainModule
import screen.setup.setupModule


val screenModules
    get() = listOf(
        setupModule,
        mainModule,
        module {
            factory<GadulkaPlayer> {
                GadulkaPlayer()
            } onClose {
                it?.release()
            }
        }
    )