package webcam

import org.koin.dsl.module
import webcam.data.DefaultVideoSource
import webcam.data.VideoSource
import webcam.data.WebCamViewModel


val webCamModule = module {
    single {
        WebCamViewModel(get())
    }
    single<VideoSource> {
        DefaultVideoSource()
    }
}