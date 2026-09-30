package eu.pretix.desktop.nfc

import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.libpretixnfc.communication.ChipReadError
import eu.pretix.libpretixsync.db.ReusableMediaType
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class NfcState {
    /** Not polling, either because nothing asked for it or because no NFC media type is active. */
    STOPPED,

    /** Polling a reader for chips. */
    RUNNING,

    /** The operating system offers no smart card service at all. */
    UNSUPPORTED,

    /** No reader is attached, or the reader chosen in the settings is not attached. */
    DISABLED,
}

sealed interface NfcReadEvent {
    data class Success(val identifier: String, val mediaType: ReusableMediaType) : NfcReadEvent
    data class Error(val error: ChipReadError) : NfcReadEvent
}

interface NfcReaderService {
    val state: StateFlow<NfcState>

    val events: SharedFlow<NfcReadEvent>

    /**
     * Starts polling for chips, or restarts it when the active media types, key sets or the chosen
     * reader changed. Does nothing while the event has no NFC-based media type.
     */
    fun start()

    fun stop()

    /**
     * Returns the readers currently attached to the system.
     */
    fun listReaders(): List<SelectableValue>

    companion object {
        /** Stands for "poll every attached reader" in the reader selection. */
        const val ANY_READER_NAME = "-"

        /** Name of the key pair the server encrypts the media key sets for. */
        const val DEVICE_KEY_NAME = "device"
    }
}
