package eu.pretix.scan.tickets.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import eu.pretix.scan.tickets.utils.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


@Composable
fun QuestionImagePreview(
    modifier: Modifier = Modifier,
    filePath: String,
    imageLoader: ImageLoader? = null
) {
    var image by remember { mutableStateOf<BufferedImage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath, imageLoader) {
        try {
            val loadedImage = if (filePath.startsWith("https")) {
                imageLoader?.loadImage(filePath)
            } else {
                withContext(Dispatchers.IO) {
                    val realPath = filePath.replaceFirst("file://", "")
                    val file = File(realPath)
                    ImageIO.read(file)
                }
            }

            loadedImage?.let {
                image = it
            } ?: run {
                errorMessage = "Failed to load image"
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            // Show a loading indicator while the image is being loaded
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            // Show an error message if something went wrong
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "Error: $errorMessage")
            }
        }

        image != null -> {
            // Display the loaded image
            Image(
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        ratio = 3f / 4f,
                        matchHeightConstraintsFirst = true,
                    ),
                painter = BitmapPainter(image!!.toComposeImageBitmap()),
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }

        else -> {
            // Fallback in case nothing is loaded
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "No image to display.")
            }
        }
    }
}
