package eu.pretix.scan.tickets.utils

import eu.pretix.libpretixsync.api.PretixApi
import java.awt.image.BufferedImage

expect class ImageLoader(api: PretixApi) {
    suspend fun loadImage(url: String): BufferedImage?
}
