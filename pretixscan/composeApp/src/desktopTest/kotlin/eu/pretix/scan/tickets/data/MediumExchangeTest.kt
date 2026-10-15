package eu.pretix.scan.tickets.data

import eu.pretix.desktop.nfc.NfcState
import eu.pretix.libpretixsync.db.MediaPolicy
import eu.pretix.libpretixsync.db.ReusableMediaType
import kotlin.test.Test
import kotlin.test.assertEquals

class MediumExchangeTest {

    @Test
    fun `reusing a UID chip is supported`() {
        assertEquals(
            ExchangeSupport.SUPPORTED,
            exchangeSupport(ReusableMediaType.NFC_UID, MediaPolicy.REUSE, NfcState.RUNNING)
        )
    }

    @Test
    fun `appending to a UID chip is supported`() {
        assertEquals(
            ExchangeSupport.SUPPORTED,
            exchangeSupport(ReusableMediaType.NFC_UID, MediaPolicy.APPEND, NfcState.RUNNING)
        )
    }

    @Test
    fun `reusing a Mifare Ultralight AES chip is supported`() {
        assertEquals(
            ExchangeSupport.SUPPORTED,
            exchangeSupport(ReusableMediaType.NFC_MF0AES, MediaPolicy.REUSE, NfcState.RUNNING)
        )
    }

    @Test
    fun `writing a new Mifare Ultralight AES chip is not implemented`() {
        for (policy in listOf(MediaPolicy.NEW, MediaPolicy.REUSE_OR_NEW, MediaPolicy.APPEND_OR_NEW)) {
            assertEquals(
                ExchangeSupport.NOT_IMPLEMENTED,
                exchangeSupport(ReusableMediaType.NFC_MF0AES, policy, NfcState.RUNNING),
                "policy $policy"
            )
        }
    }

    @Test
    fun `writing a new UID chip is supported`() {
        for (policy in listOf(MediaPolicy.NEW, MediaPolicy.REUSE_OR_NEW, MediaPolicy.APPEND_OR_NEW)) {
            assertEquals(
                ExchangeSupport.SUPPORTED,
                exchangeSupport(ReusableMediaType.NFC_UID, policy, NfcState.RUNNING),
                "policy $policy"
            )
        }
    }

    @Test
    fun `an unknown medium type is not implemented`() {
        for (type in listOf(null, ReusableMediaType.NONE, ReusableMediaType.BARCODE, ReusableMediaType.UNSUPPORTED)) {
            assertEquals(
                ExchangeSupport.NOT_IMPLEMENTED,
                exchangeSupport(type, MediaPolicy.REUSE, NfcState.RUNNING),
                "type $type"
            )
        }
    }

    @Test
    fun `writing a new medium of an unknown type is not implemented`() {
        for (type in listOf(null, ReusableMediaType.NONE, ReusableMediaType.BARCODE, ReusableMediaType.UNSUPPORTED)) {
            assertEquals(
                ExchangeSupport.NOT_IMPLEMENTED,
                exchangeSupport(type, MediaPolicy.NEW, NfcState.RUNNING),
                "type $type"
            )
        }
    }

    @Test
    fun `a missing media policy is not implemented`() {
        for (policy in listOf(null, MediaPolicy.NONE)) {
            assertEquals(
                ExchangeSupport.NOT_IMPLEMENTED,
                exchangeSupport(ReusableMediaType.NFC_UID, policy, NfcState.RUNNING),
                "policy $policy"
            )
        }
    }

    @Test
    fun `an idle or unsupported reader cannot serve an NFC exchange`() {
        for (state in listOf(NfcState.STOPPED, NfcState.UNSUPPORTED)) {
            assertEquals(
                ExchangeSupport.NO_NFC,
                exchangeSupport(ReusableMediaType.NFC_UID, MediaPolicy.REUSE, state),
                "state $state"
            )
        }
    }

    @Test
    fun `a detached reader still allows the exchange dialog to wait for one`() {
        assertEquals(
            ExchangeSupport.SUPPORTED,
            exchangeSupport(ReusableMediaType.NFC_UID, MediaPolicy.REUSE, NfcState.DISABLED)
        )
    }
}
