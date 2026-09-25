package eu.pretix.desktop.printing

import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.getUserDataFolder
import eu.pretix.libpretixsync.models.BadgeLayout
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        assertEquals(114, fontsDir.list().size)
    }

    @Test
    fun test_printing_without_selected_printer_fails_as_not_selected() {
        val sut = factoryWithBadgePrinter(null)

        assertFailsWith<BadgePrinterUnavailableException.NotSelected> {
            sut.printBadges(BadgeLayout.defaultWithLayout("[]"), JSONObject())
        }
    }

    @Test
    fun test_printing_to_missing_printer_fails_as_not_found() {
        val sut = factoryWithBadgePrinter("pretixSCAN test printer that does not exist")

        assertFailsWith<BadgePrinterUnavailableException.NotFound> {
            sut.printBadges(BadgeLayout.defaultWithLayout("[]"), JSONObject())
        }
    }

    private fun factoryWithBadgePrinter(printerName: String?): DesktopBadgeFactory {
        val appConfig = mockk<DataStoreConfigStore>(relaxed = true)
        every { appConfig.badgePrinterName } returns printerName
        return DesktopBadgeFactory(
            appConfig = appConfig,
            printerSource = PrintingSystem(),
            fileStorage = mockk(relaxed = true),
            renderer = mockk(),
            fontRegistrar = FontRegistrar()
        )
    }
}
