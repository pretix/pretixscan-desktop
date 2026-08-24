package eu.pretix.scan.setup

import kotlin.test.*

class SetupViewModelTest {

    @Test
    fun test_valid_handshake_version_1() {
        val validJson = """{"handshake_version": 1, "url": "https://pretix.eu", "token": "cogulbrsr5tbhu2w"}"""

        val result = parseHandshakeQR(validJson)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("cogulbrsr5tbhu2w", result.second)
    }

    @Test
    fun test_valid_handshake_version_0() {
        val validJson = """{"handshake_version": 0, "url": "https://example.com", "token": "test123"}"""

        val result = parseHandshakeQR(validJson)

        assertNotNull(result)
        assertEquals("https://example.com", result.first)
        assertEquals("test123", result.second)
    }

    @Test
    fun test_valid_handshake_with_extra_fields() {
        val jsonWithExtras = """{"handshake_version": 1, "url": "https://pretix.eu", "token": "abc123", "extra_field": "ignored", "another": 42}"""

        val result = parseHandshakeQR(jsonWithExtras)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("abc123", result.second)
    }

    @Test
    fun test_valid_handshake_with_http_url() {
        val jsonWithHttp = """{"handshake_version": 1, "url": "http://localhost:8000", "token": "devtoken"}"""

        val result = parseHandshakeQR(jsonWithHttp)

        assertNotNull(result)
        assertEquals("http://localhost:8000", result.first)
        assertEquals("devtoken", result.second)
    }

    @Test
    fun test_valid_handshake_with_url_with_port() {
        val jsonWithPort = """{"handshake_version": 1, "url": "https://pretix.example.com:8443", "token": "token456"}"""

        val result = parseHandshakeQR(jsonWithPort)

        assertNotNull(result)
        assertEquals("https://pretix.example.com:8443", result.first)
        assertEquals("token456", result.second)
    }

    @Test
    fun test_valid_handshake_with_url_with_path() {
        val jsonWithPath = """{"handshake_version": 1, "url": "https://pretix.eu/api/v1", "token": "xyz789"}"""

        val result = parseHandshakeQR(jsonWithPath)

        assertNotNull(result)
        assertEquals("https://pretix.eu/api/v1", result.first)
        assertEquals("xyz789", result.second)
    }

    @Test
    fun test_legacy_qr_with_version_field_is_rejected() {
        val legacyJson = """{"version": 1, "url": "https://pretix.eu", "token": "oldtoken"}"""

        val result = parseHandshakeQR(legacyJson)

        assertNull(result)
    }

    @Test
    fun test_legacy_qr_with_both_version_fields_is_rejected() {
        val legacyJson = """{"version": 1, "handshake_version": 1, "url": "https://pretix.eu", "token": "token"}"""

        val result = parseHandshakeQR(legacyJson)

        assertNull(result)
    }

    @Test
    fun test_missing_handshake_version_field() {
        val jsonNoVersion = """{"url": "https://pretix.eu", "token": "sometoken"}"""

        val result = parseHandshakeQR(jsonNoVersion)

        assertNull(result)
    }

    @Test
    fun test_handshake_version_too_high() {
        val jsonVersion2 = """{"handshake_version": 2, "url": "https://pretix.eu", "token": "futuretoken"}"""

        val result = parseHandshakeQR(jsonVersion2)

        assertNull(result)
    }

    @Test
    fun test_handshake_version_much_too_high() {
        val jsonVersion99 = """{"handshake_version": 99, "url": "https://pretix.eu", "token": "wayintothefu"}"""

        val result = parseHandshakeQR(jsonVersion99)

        assertNull(result)
    }

    @Test
    fun test_missing_url_field() {
        val jsonNoUrl = """{"handshake_version": 1, "token": "tokenonly"}"""

        val result = parseHandshakeQR(jsonNoUrl)

        assertNull(result)
    }

    @Test
    fun test_missing_token_field() {
        val jsonNoToken = """{"handshake_version": 1, "url": "https://pretix.eu"}"""

        val result = parseHandshakeQR(jsonNoToken)

        assertNull(result)
    }

    @Test
    fun test_missing_url_and_token_fields() {
        val jsonNoFields = """{"handshake_version": 1}"""

        val result = parseHandshakeQR(jsonNoFields)

        assertNull(result)
    }

    @Test
    fun test_invalid_json_syntax_missing_closing_brace() {
        val invalidJson = """{"handshake_version": 1, "url": "https://pretix.eu", "token": "test123" """

        val result = parseHandshakeQR(invalidJson)

        assertNull(result)
    }

    @Test
    fun test_invalid_json_syntax_missing_quotes() {
        val invalidJson = """{handshake_version: 1, url: "https://pretix.eu", token: "test"}"""

        val result = parseHandshakeQR(invalidJson)

        assertNull(result)
    }

    @Test
    fun test_invalid_json_syntax_trailing_comma() {
        val invalidJson = """{"handshake_version": 1, "url": "https://pretix.eu", "token": "test",}"""

        val result = parseHandshakeQR(invalidJson)

        assertNull(result)
    }

    @Test
    fun test_empty_string() {
        val result = parseHandshakeQR("")

        assertNull(result)
    }

    @Test
    fun test_whitespace_only() {
        val result = parseHandshakeQR("   ")

        assertNull(result)
    }

    @Test
    fun test_non_json_string_ticket_barcode() {
        val ticketBarcode = "ABCD1234567890XYZ"

        val result = parseHandshakeQR(ticketBarcode)

        assertNull(result)
    }

    @Test
    fun test_non_json_string_random_text() {
        val randomText = "This is just a random string"

        val result = parseHandshakeQR(randomText)

        assertNull(result)
    }

    @Test
    fun test_url_field_is_null() {
        val jsonNullUrl = """{"handshake_version": 1, "url": null, "token": "test"}"""

        val result = parseHandshakeQR(jsonNullUrl)

        assertNull(result)
    }

    @Test
    fun test_token_field_is_null() {
        val jsonNullToken = """{"handshake_version": 1, "url": "https://pretix.eu", "token": null}"""

        val result = parseHandshakeQR(jsonNullToken)

        assertNull(result)
    }

    @Test
    fun test_both_url_and_token_are_null() {
        val jsonNullFields = """{"handshake_version": 1, "url": null, "token": null}"""

        val result = parseHandshakeQR(jsonNullFields)

        assertNull(result)
    }

    @Test
    fun test_handshake_version_is_null() {
        val jsonNullVersion = """{"handshake_version": null, "url": "https://pretix.eu", "token": "test"}"""

        val result = parseHandshakeQR(jsonNullVersion)

        assertNull(result)
    }

    @Test
    fun test_json_array_instead_of_object() {
        val jsonArray = """[{"handshake_version": 1, "url": "https://pretix.eu", "token": "test"}]"""

        val result = parseHandshakeQR(jsonArray)

        assertNull(result)
    }

    @Test
    fun test_empty_json_object() {
        val emptyJson = """{}"""

        val result = parseHandshakeQR(emptyJson)

        assertNull(result)
    }

    @Test
    fun test_url_field_is_empty_string() {
        val jsonEmptyUrl = """{"handshake_version": 1, "url": "", "token": "test"}"""

        val result = parseHandshakeQR(jsonEmptyUrl)

        assertNotNull(result)
        assertEquals("", result.first)
        assertEquals("test", result.second)
    }

    @Test
    fun test_token_field_is_empty_string() {
        val jsonEmptyToken = """{"handshake_version": 1, "url": "https://pretix.eu", "token": ""}"""

        val result = parseHandshakeQR(jsonEmptyToken)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("", result.second)
    }

    @Test
    fun test_handshake_version_is_string_not_number() {
        val jsonVersionString = """{"handshake_version": "1", "url": "https://pretix.eu", "token": "test"}"""

        val result = parseHandshakeQR(jsonVersionString)

        assertNull(result)
    }

    @Test
    fun test_handshake_version_is_negative() {
        val jsonNegativeVersion = """{"handshake_version": -1, "url": "https://pretix.eu", "token": "test"}"""

        val result = parseHandshakeQR(jsonNegativeVersion)

        assertNull(result)
    }

    @Test
    fun test_url_with_special_characters() {
        val jsonSpecialUrl = """{"handshake_version": 1, "url": "https://pretix.example.com/path?param=value&foo=bar", "token": "test123"}"""

        val result = parseHandshakeQR(jsonSpecialUrl)

        assertNotNull(result)
        assertEquals("https://pretix.example.com/path?param=value&foo=bar", result.first)
        assertEquals("test123", result.second)
    }

    @Test
    fun test_token_with_special_characters() {
        val jsonSpecialToken = """{"handshake_version": 1, "url": "https://pretix.eu", "token": "test-token_123.abc"}"""

        val result = parseHandshakeQR(jsonSpecialToken)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("test-token_123.abc", result.second)
    }

    @Test
    fun test_json_with_different_field_order() {
        val jsonDifferentOrder = """{"token": "mytoken", "handshake_version": 1, "url": "https://pretix.eu"}"""

        val result = parseHandshakeQR(jsonDifferentOrder)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("mytoken", result.second)
    }

    @Test
    fun test_json_with_whitespace() {
        val jsonWithWhitespace = """{
            "handshake_version": 1,
            "url": "https://pretix.eu",
            "token": "test123"
        }"""

        val result = parseHandshakeQR(jsonWithWhitespace)

        assertNotNull(result)
        assertEquals("https://pretix.eu", result.first)
        assertEquals("test123", result.second)
    }

    private fun parseHandshakeQR(scannedText: String): Pair<String, String>? {
        return SetupViewModel.parseHandshakeQR(scannedText)
    }
}
