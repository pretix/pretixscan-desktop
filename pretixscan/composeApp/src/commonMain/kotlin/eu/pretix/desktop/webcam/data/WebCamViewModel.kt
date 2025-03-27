package eu.pretix.desktop.webcam.data

import androidx.lifecycle.ViewModel
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamException
import com.github.sarxos.webcam.util.ImageUtils
import eu.pretix.desktop.cache.getUserDataFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.imageio.ImageIO

class WebCamViewModel(
    private val videoSource: VideoSource
) : ViewModel() {
    private val log = Logger.getLogger("eu/pretix/desktop/webcam")

    val availableImageData: StateFlow<ImageData?> = videoSource.collectAvailableImageData()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val noSelectedVideo = Video(
        name = "-",
        availableResolutions = listOf(),
        fps = .0,
    )

    private val _uiState: MutableStateFlow<CameraState> = MutableStateFlow(
        defaultCameraState(
            selectedVideo = noSelectedVideo
        )
    )
    val uiState: StateFlow<CameraState> = _uiState

    suspend fun load() {
        configureWebCam()
        observeVideoInput()
    }


    suspend fun observeVideoInput() = coroutineScope.launch {
        videoSource.getAvailableWebcam().collectLatest { videos ->
            val newVideos = listOf(noSelectedVideo) + videos.map { webcam ->
                Video(
                    name = webcam.name,
                    availableResolutions = webcam.device.resolutions.map { it.toResolution() },
                    fps = webcam.fps,
                )
            }
            if (_uiState.value.availableVideos != newVideos) {
                _uiState.update { state ->
                    state.copy(availableVideos = newVideos)
                }
                if (_uiState.value.preselectCamera && _uiState.value.selectedVideo?.name == "-") {
                    val newCamera = newVideos.firstOrNull { it.name != "-" }
                    if (newCamera != null) {
                        selectVideo(newCamera)
                    }
                }
            }
        }
    }

    fun stop() {
        videoSource.close()
    }

    fun selectVideo(video: Video) {
        if (video.name == _uiState.value.selectedVideo?.name) return
        if (video.name == "-") {
            videoSource.close()
            _uiState.update { it.copy(selectedVideo = noSelectedVideo) }
            return
        }
        videoSource.open(video.name)
        _uiState.update {
            it.copy(selectedVideo = video, preselectCamera = false)
        }
    }

    fun savePhoto(): String? {
        val imageData = videoSource.takeScreenshot()
        if (imageData == null) {
            log.warning("Unable to save a photo without a buffered image from the camera")
            return null
        }

        val dir = File(getUserDataFolder(), "photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val photoFile = File(
            dir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".png"
        )
        try {
            ImageIO.write(imageData, ImageUtils.FORMAT_PNG, photoFile)
            return photoFile.absolutePath
        } catch (e: IOException) {
            throw WebcamException(e)
        }
    }


    fun listWebCams() {
        val webcams = Webcam.getWebcams()
        for (webcam in webcams) {
            log.info("Found camera ${webcam.name}")
        }
    }
}

data class Video(
    val name: String,
    val availableResolutions: List<Resolution>,
    val fps: Double,
) {
    data class Resolution(
        val width: Int,
        val height: Int,
    )
}

fun Dimension.toResolution() = Video.Resolution(width = this.width, height = this.height)

fun Video.Resolution.toDimension() = Dimension(this.width, this.height)


fun defaultCameraState(
    selectedVideo: Video?,
) = CameraState(
    selectedVideo = selectedVideo,
    availableVideos = null
)

data class CameraState(
    val selectedVideo: Video?,
    val availableVideos: List<Video>?,
    val preselectCamera: Boolean = true
)