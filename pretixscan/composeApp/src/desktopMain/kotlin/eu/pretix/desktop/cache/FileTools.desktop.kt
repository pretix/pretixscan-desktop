package eu.pretix.desktop.cache

import java.awt.Desktop
import java.io.File

actual fun openPathInFileBrowser(path: String) {
    Desktop.getDesktop().open(File(path))
}