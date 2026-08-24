package eu.pretix.desktop.printing

import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.scan.settings.data.PrinterSource
import org.jetbrains.compose.resources.getString
import pretixscan.composeapp.generated.resources.Res
import pretixscan.composeapp.generated.resources.settings_printer_layout_auto
import pretixscan.composeapp.generated.resources.settings_printer_layout_landscape
import pretixscan.composeapp.generated.resources.settings_printer_layout_portrait
import javax.print.DocFlavor
import javax.print.PrintServiceLookup

class PrintingSystem : PrinterSource {
    override fun listPrinters(): List<SelectableValue> {
        return PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .map {
                SelectableValue(
                    value = it.name,
                    label = it.name
                )
            }
    }


    override fun selectOption(name: String?): SelectableValue? {
        if (name.isNullOrBlank()) {
            return null
        }
        val service = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null)
            .firstOrNull {
                it.name == name
            }
        if (service != null) {
            return SelectableValue(value = service.name, label = service.name)
        }
        return null
    }

    override suspend fun listPrinterOrientations(): List<SelectableValue> {
        return listOf(
            SelectableValue(
                value = "Auto",
                label = getString(Res.string.settings_printer_layout_auto)
            ),
            SelectableValue(
                value = "Landscape",
                label = getString(Res.string.settings_printer_layout_landscape)
            ),
            SelectableValue(
                value = "Portrait",
                label = getString(Res.string.settings_printer_layout_portrait)
            )
        )
    }

    override suspend fun selectPrinterOrientation(value: String?): SelectableValue? {
        if (value.isNullOrBlank()) {
            return null
        }

        return when (value) {
            "Auto" -> {
                SelectableValue(
                    value = "Auto",
                    label = getString(Res.string.settings_printer_layout_auto)
                )
            }

            "Landscape" -> {
                SelectableValue(
                    value = "Landscape",
                    label = getString(Res.string.settings_printer_layout_landscape)
                )
            }

            "Portrait" -> {
                SelectableValue(
                    value = "Portrait",
                    label = getString(Res.string.settings_printer_layout_portrait)
                )
            }

            else -> {
                null
            }
        }
    }
}
