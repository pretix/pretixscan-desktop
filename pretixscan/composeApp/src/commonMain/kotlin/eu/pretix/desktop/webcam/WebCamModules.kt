package eu.pretix.desktop.webcam

import eu.pretix.desktop.webcam.data.DefaultVideoSource
import eu.pretix.desktop.webcam.data.VideoSource
import eu.pretix.desktop.webcam.data.WebCamViewModel
import org.koin.dsl.module


val webCamModule = module {
    single {
        WebCamViewModel(get())
    }
    single<VideoSource> {
        DefaultVideoSource()
    }
}