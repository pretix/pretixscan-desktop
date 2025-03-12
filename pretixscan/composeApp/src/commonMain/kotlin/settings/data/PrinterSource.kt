package settings.data

import app.ui.KeyValueOption

interface PrinterSource {
    /**
     * Returns a list of printer services suitable for badge printing.
     */
    fun listPrinters(): List<KeyValueOption>

    /**
     * Resolves the printer service corresponding to the provided name if it's available on the system.
     */
    fun selectOption(name: String?): KeyValueOption?

    suspend fun listPrinterOrientations(): List<KeyValueOption>

    suspend fun selectPrinterOrientation(value: String?): KeyValueOption?
}

