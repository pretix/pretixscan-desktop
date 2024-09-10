package screen

import screen.main.mainModule
import screen.setup.setupModule


val screenModules
    get() = listOf(
        setupModule,
        mainModule
    )