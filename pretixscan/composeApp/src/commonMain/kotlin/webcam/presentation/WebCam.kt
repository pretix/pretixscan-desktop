package webcam.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.ui.CustomColor
import app.ui.asColor
import kotlinx.coroutines.delay
import main.presentation.toolbar.Logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.ic_photo_camera_white_24
import pretixscan.composeapp.generated.resources.none_camera
import pretixscan.composeapp.generated.resources.select_camera
import webcam.data.ImageData
import webcam.data.Video
import webcam.data.WebCamViewModel

@Composable
fun WebCam(onPhotoTaken: (String?) -> Unit) {
    val viewModel = koinViewModel<WebCamViewModel>()

    val uiState by viewModel.uiState.collectAsState()
    val availableImageData by viewModel.availableImageData.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val selectedVideo = uiState.selectedVideo
    val availableVideo = uiState.availableVideos

    LaunchedEffect(Unit) {
        delay(50)
        viewModel.load()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stop()
        }
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                selectedDevice = selectedVideo?.name,
                availableDeviceNames = availableVideo?.map { it.name },
                onDeviceSelect = { selectedDevice ->
                    availableVideo
                        ?.find { it.name == selectedDevice }
                        ?.let { viewModel.selectVideo(it) }
                }
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                VideoSurface(
                    selectedVideo = selectedVideo,
                    availableImageData = availableImageData,
                )

                Column {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val path = viewModel.savePhoto()
                        onPhotoTaken(path)
                    }) {
                        Image(
                            painter = painterResource(Res.drawable.ic_photo_camera_white_24),
                            contentDescription = stringResource(Res.string.take_a_photo)
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun Toolbar(
    selectedDevice: String?,
    availableDeviceNames: List<String>?,
    onDeviceSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp)
    ) {
        Logo()
        Spacer(Modifier.weight(1f))

        if (availableDeviceNames != null) {
            Box(contentAlignment = Alignment.TopStart) {
                Button(modifier = Modifier.padding(horizontal = 16.dp), onClick = { expanded = true }) {
                    Row {
                        if (selectedDevice != "-") {
                            Text(selectedDevice ?: "")
                        } else {
                            Text(stringResource(Res.string.select_camera))
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select camera",
                            tint = CustomColor.White.asColor()
                        )
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableDeviceNames.forEachIndexed { _, name ->
                        DropdownMenuItem(
                            text = {
                                if (name == "-") {
                                    Text(stringResource(Res.string.none_camera))
                                } else {
                                    Text(name)
                                }
                            },
                            onClick = {
                                onDeviceSelect(name)
                                expanded = false
                            },
                            leadingIcon = {
                                if (name == selectedDevice) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                } else if (name == "-") {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                } else {
                                    Icon(Icons.Default.Face, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = availableDeviceNames == null) {
            DeviceListLoading()
        }
    }
}

@Composable
private fun VideoSurface(
    selectedVideo: Video?,
    availableImageData: ImageData?,
) {
    if (selectedVideo?.name == "-") return
    if (availableImageData == null) return
    val imageRatio = availableImageData.image.width.toFloat() / availableImageData.image.height
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(
                    ratio = imageRatio,
                    matchHeightConstraintsFirst = true,
                ),
            painter = BitmapPainter(availableImageData.image.toComposeImageBitmap()),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }
}

@Composable
private fun DeviceListLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        LinearProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
    }
}