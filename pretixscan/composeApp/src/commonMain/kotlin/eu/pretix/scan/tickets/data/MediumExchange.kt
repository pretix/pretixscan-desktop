package eu.pretix.scan.tickets.data

import eu.pretix.desktop.nfc.NfcState
import eu.pretix.libpretixsync.db.MediaPolicy
import eu.pretix.libpretixsync.db.ReusableMediaType

enum class ExchangeSupport {
    SUPPORTED,

    /** The medium the server asks for cannot be written by this app. */
    NOT_IMPLEMENTED,

    /** An NFC medium is needed but no reader can be polled. */
    NO_NFC,
}

/**
 * Decides whether a ticket can be exchanged into the medium the server asks for.
 *
 * A policy that allows a new medium only works for UID chips, because writing a Mifare Ultralight
 * AES chip is not implemented.
 */
fun exchangeSupport(
    type: ReusableMediaType?,
    policy: MediaPolicy?,
    nfcState: NfcState
): ExchangeSupport {
    val knownType = type == ReusableMediaType.NFC_UID || type == ReusableMediaType.NFC_MF0AES

    val supported = when (policy) {
        MediaPolicy.NEW,
        MediaPolicy.REUSE_OR_NEW,
        MediaPolicy.APPEND_OR_NEW -> type == ReusableMediaType.NFC_UID

        MediaPolicy.REUSE,
        MediaPolicy.APPEND -> knownType

        else -> false
    }

    if (!supported) {
        return ExchangeSupport.NOT_IMPLEMENTED
    }

    if (type?.isNfcBased() == true) {
        when (nfcState) {
            NfcState.STOPPED,
            NfcState.UNSUPPORTED -> return ExchangeSupport.NO_NFC

            NfcState.RUNNING,
            NfcState.DISABLED -> {}
        }
    }

    return ExchangeSupport.SUPPORTED
}
