package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.getUserDataFolder
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BadgeFactoryTest {

    @Test
    fun test_fonts_can_be_exported_for_printing() = runTest {
        // arrange
        // house-keeping, we need a data folder and make sure it's empty
        val dataFolder = getUserDataFolder()
        val fontsDir = File(dataFolder, "files/fonts")
        if (fontsDir.exists()) {
            fontsDir.deleteRecursively()
        }
        fontsDir.mkdirs()
        assertTrue { fontsDir.isDirectory }
        assertEquals(0, fontsDir.list().size)
        val sut = FontRegistrar()

        // do
        sut.exportAndRegisterAllFonts(dataFolder)
        assertEquals(74, fontsDir.list().size)
    }
}
