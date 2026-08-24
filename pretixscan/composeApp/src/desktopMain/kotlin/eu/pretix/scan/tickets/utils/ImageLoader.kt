package eu.pretix.scan.tickets.utils

import eu.pretix.libpretixsync.api.PretixApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

actual class ImageLoader actual constructor(private val api: PretixApi) {
    private val cache = ConcurrentHashMap<String, BufferedImage>()

    actual suspend fun loadImage(url: String): BufferedImage? {
        cache[url]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val apiResponse = api.downloadFile(url)
                apiResponse.response.use { response ->
                    val body = response.body
                    if (response.isSuccessful && body != null) {
                        val image = body.byteStream().use { inputStream ->
                            ImageIO.read(inputStream)
                        }
                        if (image != null) {
                            cache[url] = image
                        }
                        image
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
