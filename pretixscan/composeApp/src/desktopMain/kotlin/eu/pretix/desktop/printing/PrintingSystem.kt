package eu.pretix.desktop.printing

import app.ui.KeyValueOption
import org.jetbrains.compose.resources.getString
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.settings_printer_layout_auto
import pretixscan.composeapp.generated.resources.settings_printer_layout_landscape
import pretixscan.composeapp.generated.resources.settings_printer_layout_portrait
import settings.data.PrinterSource
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

class PrintingSystem : PrinterSource {
    override fun listPrinters(): List<KeyValueOption> {
        return PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .map {
                KeyValueOption(
                    key = it.name,
                    value = it.name
                )
            }
    }


    override fun selectOption(name: String?): KeyValueOption? {
        if (name.isNullOrBlank()) {
            return null
        }
        val service = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .firstOrNull {
                it.name == name
            }
        if (service != null) {
            return KeyValueOption(key = service.name, value = service.name)
        }
        return null
    }

    override suspend fun listPrinterOrientations(): List<KeyValueOption> {
        return listOf(
            KeyValueOption(
                key = "Auto",
                value = getString(Res.string.settings_printer_layout_auto)
            ),
            KeyValueOption(
                key = "Landscape",
                value = getString(Res.string.settings_printer_layout_landscape)
            ),
            KeyValueOption(
                key = "Portrait",
                value = getString(Res.string.settings_printer_layout_portrait)
            )
        )
    }

    override suspend fun selectPrinterOrientation(value: String?): KeyValueOption? {
        if (value.isNullOrBlank()) {
            return null
        }

        return when (value) {
            "Auto" -> {
                KeyValueOption(
                    key = "Auto",
                    value = getString(Res.string.settings_printer_layout_auto)
                )
            }

            "Landscape" -> {
                KeyValueOption(
                    key = "Landscape",
                    value = getString(Res.string.settings_printer_layout_landscape)
                )
            }

            "Portrait" -> {
                KeyValueOption(
                    key = "Portrait",
                    value = getString(Res.string.settings_printer_layout_portrait)
                )
            }

            else -> {
                null
            }
        }
    }
}
