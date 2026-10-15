package eu.pretix.desktop.nfc

import eu.pretix.libpretixnfc.cryptography.An10922KeyDiversification
import eu.pretix.libpretixnfc.decodeHex
import eu.pretix.libpretixnfc.toHexString
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyDiversificationTest {

    @Test
    fun `diversifies the AN10922 example key`() {
        // example from https://www.nxp.com/docs/en/application-note/AN10922.pdf
        val key = An10922KeyDiversification().generateDiversifiedKeyAES128(
            "00112233445566778899AABBCCDDEEFF".decodeHex(),
            "04782E21801D80".decodeHex(),
            "3042F5".decodeHex(),
            "4E585020416275".decodeHex(),
        )

        assertEquals("A8DD63A3B89D54B37CA802473FDA9175", key.toHexString())
    }
}
