package eu.pretix.desktop.webcam.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.Logo
import eu.pretix.desktop.app.ui.Tooltip
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.desktop.webcam.data.ImageData
import eu.pretix.desktop.webcam.data.Video
import eu.pretix.desktop.webcam.data.VideoSource
import eu.pretix.desktop.webcam.data.WebCamViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pretixscan.composeapp.generated.resources.*

@Composable
fun WebCam(onCancel: () -> Unit, onPhotoTaken: (String?) -> Unit) {
    val viewModel = koinViewModel<WebCamViewModel>()

    val uiState by viewModel.uiState.collectAsState()
    val availableImageData by viewModel.availableImageData.collectAsState()

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

    val focusRequester = remember { FocusRequester() }
    val canTakePhoto = selectedVideo != null && selectedVideo.name != VideoSource.NO_CAMERA_NAME

    Surface(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (keyEvent.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        if (canTakePhoto) {
                            onPhotoTaken(viewModel.savePhoto())
                            true
                        } else false
                    }
                    else -> false
                }
            }
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                selectedDevice = selectedVideo?.name,
                availableDeviceNames = availableVideo?.map { it.name },
                onDeviceSelect = { selectedDevice ->
                    availableVideo
                        ?.find { it.name == selectedDevice }
                        ?.let { viewModel.selectVideo(it) }
                },
                onCancel = onCancel
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                VideoSurface(
                    selectedVideo = selectedVideo,
                    availableImageData = availableImageData,
                )

                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    when {
                        canTakePhoto -> {
                            Tooltip(stringResource(Res.string.take_a_photo)) {
                                IconButton(
                                    modifier = Modifier
                                        .background(CustomColor.BrandDark.asColor(), CircleShape)
                                        .padding(8.dp),
                                    onClick = {
                                        val path = viewModel.savePhoto()
                                        onPhotoTaken(path)
                                    }
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.ic_photo_camera_white_24),
                                        contentDescription = stringResource(Res.string.take_a_photo),
                                        colorFilter = ColorFilter.tint(CustomColor.White.asColor())
                                    )
                                }
                            }
                        }
                        availableVideo?.isEmpty() == true -> {
                            Text(
                                text = stringResource(Res.string.no_available_cameras),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
    onDeviceSelect: (String) -> Unit,
    onCancel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CustomColor.BrandDark.asColor())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo()
        Spacer(Modifier.weight(1f))

        if (availableDeviceNames != null) {
            if (availableDeviceNames.isNotEmpty() && selectedDevice != VideoSource.NO_CAMERA_NAME) {
                Box(contentAlignment = Alignment.TopStart) {
                    Button(modifier = Modifier.padding(horizontal = 16.dp), onClick = { expanded = true }) {
                        Row {
                            Text(selectedDevice ?: stringResource(Res.string.select_camera))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = CustomColor.White.asColor()
                            )
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableDeviceNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onDeviceSelect(name)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (name == selectedDevice) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    } else {
                                        Icon(Icons.Default.Face, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        } else {
            DeviceListLoading()
        }

        Tooltip(stringResource(Res.string.cancel)) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.cancel),
                    tint = CustomColor.White.asColor()
                )
            }
        }
    }
}

@Composable
private fun VideoSurface(
    selectedVideo: Video?,
    availableImageData: ImageData?,
) {
    if (selectedVideo?.name == VideoSource.NO_CAMERA_NAME) return
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