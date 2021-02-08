package eu.pretix.pretixscan.desktop

import eu.pretix.libpretixsync.sync.FileStorage
import java.io.File
import java.io.FilenameFilter
import java.io.OutputStream

class DesktopFileStorage(private val dataDir: File) : FileStorage {

    fun getDir(): File {
        val dir = File(dataDir, "dbcache")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    override fun contains(filename: String): Boolean {
        return File(getDir(), filename).exists()
    }

    fun getFile(filename: String): File {
        return File(getDir(), filename)
    }

    override fun writeStream(filename: String): OutputStream? {
        return File(getDir(), filename).outputStream()
    }

    override fun listFiles(filter: FilenameFilter?): Array<String> {
        return getDir().list(filter)
    }

    override fun delete(filename: String) {
        File(getDir(), filename).delete()
    }
}

