package webcam.data

// The following implementation is based on https://github.com/akexorcist/backdrop

import com.github.sarxos.webcam.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.awt.Dimension
import java.awt.image.BufferedImage


interface VideoSource {
    fun getAvailableWebcam(): Flow<List<Webcam>>

    fun open(name: String, dimension: Dimension? = null)

    fun close()

    fun collectCurrentWebcam(): Flow<Webcam?>

    fun collectAvailableImageData(): StateFlow<ImageData?>

    fun takeScreenshot(): BufferedImage?
}

sealed class VideoState {
    object Open : VideoState()

    object Closed : VideoState()

    object Disposed : VideoState()

    object ImageObtained : VideoState()
}

data class ImageData(
    val image: BufferedImage,
    val timestamp: Long,
    val frameRate: Double,
)

class DefaultVideoSource : VideoSource {

    private var currentVideo: MutableStateFlow<Webcam?> = MutableStateFlow(null)
    private val videoEventFlow: MutableStateFlow<VideoState> = MutableStateFlow(VideoState.Closed)
    private val availableImageDataFlow: MutableStateFlow<ImageData?> = MutableStateFlow(null)

    override fun getAvailableWebcam(): Flow<List<Webcam>> = callbackFlow {
        trySend(Webcam.getWebcams())
        val listener = object : WebcamDiscoveryListener {
            override fun webcamFound(event: WebcamDiscoveryEvent?) {
                trySend(Webcam.getWebcams())
            }

            override fun webcamGone(event: WebcamDiscoveryEvent?) {
                trySend(Webcam.getWebcams())
            }
        }
        Webcam.addDiscoveryListener(listener)
        awaitClose { Webcam.removeDiscoveryListener(listener) }
    }

    override fun open(name: String, dimension: Dimension?) {
        currentVideo.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        Webcam.getWebcams().find { it.name == name }?.let { webcam ->
            (dimension ?: webcam.device.resolutions.getOrNull(0))?.let {
                webcam.device.resolution = it
            }
            webcam.open(true)
            webcam.addWebcamListener(webcamListener)
            currentVideo.update { webcam }
        }
    }

    override fun close() {
        currentVideo.value?.takeIf { it.isOpen }
            ?.let {
                it.close()
                it.removeWebcamListener(webcamListener)
            }
        currentVideo.update { null }
    }

    override fun collectCurrentWebcam(): Flow<Webcam?> = currentVideo

    override fun collectAvailableImageData(): StateFlow<ImageData?> = availableImageDataFlow
    override fun takeScreenshot(): BufferedImage? {
        return currentVideo.value?.takeIf { it.isOpen }
            ?.let {
                return cropToAspectRatio(it.image, 3.0 / 4.0)
            }
    }

    private val webcamListener = object : WebcamListener {
        override fun webcamOpen(event: WebcamEvent) {
            videoEventFlow.update {
                VideoState.Open
            }
        }

        override fun webcamClosed(event: WebcamEvent) {
            videoEventFlow.update {
                VideoState.Closed
            }
        }

        override fun webcamDisposed(event: WebcamEvent) {
            videoEventFlow.update {
                VideoState.Disposed
            }
        }

        override fun webcamImageObtained(event: WebcamEvent) {
            videoEventFlow.update {
                VideoState.ImageObtained
            }
            availableImageDataFlow.update {
                ImageData(
                    image = cropToAspectRatio(event.image, 3.0 / 4.0),
                    timestamp = System.currentTimeMillis(),
                    frameRate = currentVideo.value?.fps ?: 0.0
                )
            }
        }
    }
}

 fun cropToAspectRatio(
            image: BufferedImage,
            targetAspectRatio: Double
        ): BufferedImage {
            val width = image.width
            val height = image.height

            val cropHeight = height // not rotating the image
            val cropWidth = (height * targetAspectRatio).toInt()
            return image.getSubimage((width - cropWidth) / 2, 0, cropWidth, cropHeight)
        }