package eu.pretix.scan.status

import eu.pretix.scan.status.presentation.StatusScreenViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal val statusModule
    get() = module {
        factoryOf(::StatusScreenViewModel)
    }