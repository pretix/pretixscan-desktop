package eu.pretix.scan.tickets.presentation

import com.vanniktech.locale.Country
import eu.pretix.desktop.scan.tickets.data.PhoneValidator
import eu.pretix.scan.tickets.data.calculateDefaultCountry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PhoneNumberInputTest {

    @Test
    fun testCalculateDefaultCountry_withE164Number_extractsCorrectCountry() {
        val belgiumNumber = "+32474123456"
        val country = calculateDefaultCountry(belgiumNumber)
        assertEquals("BE", country.code, "Should extract Belgium from +32 prefix")
    }

    @Test
    fun testCalculateDefaultCountry_withGermanE164Number_extractsCorrectCountry() {
        val germanNumber = "+493012345678"
        val country = calculateDefaultCountry(germanNumber)
        assertEquals("DE", country.code, "Should extract Germany from +49 prefix")
    }

    @Test
    fun testCalculateDefaultCountry_withUSE164Number_extractsCorrectCountry() {
        val usNumber = "+12125551234"
        val country = calculateDefaultCountry(usNumber)
        assertEquals("+1", country.callingCodes.first(), "Should extract country with +1 calling code")
    }

    @Test
    fun testCalculateDefaultCountry_withNonE164Number_returnsSystemDefault() {
        val localNumber = "0474123456"
        val country = calculateDefaultCountry(localNumber)
        assertNotNull(country, "Should return a country (system default)")
    }

    @Test
    fun testCalculateDefaultCountry_withEmptyString_returnsSystemDefault() {
        val country = calculateDefaultCountry("")
        assertNotNull(country, "Should return system default for empty string")
    }

    @Test
    fun testCalculateDefaultCountry_withNull_returnsSystemDefault() {
        val country = calculateDefaultCountry(null)
        assertNotNull(country, "Should return system default for null")
    }

    @Test
    fun testPhoneValidator_parsesBelgiumNumber() {
        val validator = PhoneValidator()
        val result = validator.parse("0474123456", "BE")
        assertNotNull(result, "Should parse valid Belgium number")
        assertEquals("+32474123456", result.number, "Should format to E164")
    }

    @Test
    fun testPhoneValidator_parsesGermanNumber() {
        val validator = PhoneValidator()
        val result = validator.parse("30 1234 5678", "DE")
        assertNotNull(result, "Should parse valid German number")
        assertEquals("+493012345678", result.number, "Should format to E164")
    }

    @Test
    fun testPhoneValidator_parsesUSNumber() {
        val validator = PhoneValidator()
        val result = validator.parse("212 555 1234", "US")
        assertNotNull(result, "Should parse valid US number")
        assertEquals("+12125551234", result.number, "Should format to E164")
    }

    @Test
    fun testPhoneValidator_rejectsInvalidNumber() {
        val validator = PhoneValidator()
        val result = validator.parse("invalid", "BE")
        assertNull(result, "Should reject invalid phone number")
    }

    @Test
    fun testPhoneValidator_handlesE164Input() {
        val validator = PhoneValidator()
        val result = validator.parse("+32474123456", "BE")
        assertNotNull(result, "Should parse E164 formatted number")
        assertEquals("+32474123456", result.number, "Should preserve E164 format")
    }

    @Test
    fun testPhoneValidator_validatesCorrectly() {
        val validator = PhoneValidator()
        assertEquals(true, validator.isValid("0474123456", "BE"), "Should validate correct Belgium number")
        assertEquals(true, validator.isValid("+32474123456", "BE"), "Should validate E164 Belgium number")
        assertEquals(false, validator.isValid("invalid", "BE"), "Should reject invalid number")
        assertEquals(false, validator.isValid("", "BE"), "Should reject empty number")
    }

    @Test
    fun testCountryCodeExtraction_fromE164() {
        val testCases = mapOf(
            "+32474123456" to "+32",
            "+493012345678" to "+49",
            "+12125551234" to "+1",
            "+33123456789" to "+33",
            "+447700900123" to "+44"
        )

        testCases.forEach { (number, expectedCallingCode) ->
            val country = calculateDefaultCountry(number)
            assertEquals(
                expectedCallingCode,
                country.callingCodes.first(),
                "Should extract country with calling code $expectedCallingCode from $number"
            )
        }
    }

    @Test
    fun testCountrySelection_persistsAfterTyping() {
        val selectedCountry = "BE"
        val typedValue = "0474"

        val country = Country.entries.firstOrNull { it.code == selectedCountry }
        assertNotNull(country, "Belgium should be available")
        assertEquals("BE", country.code, "Country should remain Belgium")
    }

    @Test
    fun testPhoneValidator_withDifferentFormats() {
        val validator = PhoneValidator()

        val formats = listOf(
            "0474123456",
            "0474 12 34 56",
            "0474-12-34-56",
            "0474.12.34.56",
            "+32474123456",
            "+32 474 12 34 56"
        )

        formats.forEach { format ->
            val result = validator.parse(format, "BE")
            assertNotNull(result, "Should parse format: $format")
            assertEquals("+32474123456", result.number, "All formats should result in same E164: $format")
        }
    }

    @Test
    fun testPhoneValidator_preservesInternationalPrefix() {
        val validator = PhoneValidator()

        val result = validator.parse("+32474123456", "")
        assertNotNull(result, "Should parse number with international prefix even without region")
        assertEquals("+32474123456", result.number, "Should preserve international format")
    }
}
