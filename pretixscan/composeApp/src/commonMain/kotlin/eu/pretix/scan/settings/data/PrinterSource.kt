package eu.pretix.scan.settings.data

import eu.pretix.desktop.app.ui.SelectableValue

interface PrinterSource {
    /**
     * Returns a list of printer services suitable for badge printing.
     */
    fun listPrinters(): List<SelectableValue>

    /**
     * Resolves the printer service corresponding to the provided name if it's available on the system.
     */
    fun selectOption(name: String?): SelectableValue?

    suspend fun listPrinterOrientations(): List<SelectableValue>

    suspend fun selectPrinterOrientation(value: String?): SelectableValue?
}

