package eu.pretix.desktop.scan.tickets.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PhoneValidatorTest {

    @Test
    fun empty_string_not_a_valid_phone() = runTest {
        val sut = PhoneValidator()
        assertEquals(false, sut.isValid(" ", null))
        assertEquals(false, sut.isValid("   ", null))
        assertEquals(false, sut.isValid("", null))
    }

    @Test
    fun formats_national_into_international_phone() = runTest {
        val sut = PhoneValidator()
        val phone = "0474 12 34 56"
        val region = "BE"
        assertEquals(true, sut.isValid(phone, region))
        val parsed = sut.parse(phone, region)
        assertNotNull(parsed)
        assertEquals("+32474123456", parsed.number)
        assertEquals("0474 12 34 56", parsed.rawValue)
    }

    @Test
    fun cleanup_international_phone() = runTest {
        val sut = PhoneValidator()
        val phone = "+49 30 1234.5678"
        val region = "DE"
        assertEquals(true, sut.isValid(phone, region))
        val parsed = sut.parse(phone, region)
        assertNotNull(parsed)
        assertEquals("+493012345678", parsed.number)
        assertEquals("+49 30 1234.5678", parsed.rawValue)
    }

    @Test
    fun null_region_code_with_international_format() = runTest {
        val sut = PhoneValidator()
        val phone = "+32474123456"
        assertEquals(true, sut.isValid(phone, null))
        val parsed = sut.parse(phone, null)
        assertNotNull(parsed)
        assertEquals("+32474123456", parsed.number)
    }

    @Test
    fun empty_region_code_with_international_format() = runTest {
        val sut = PhoneValidator()
        val phone = "+32474123456"
        assertEquals(true, sut.isValid(phone, ""))
        val parsed = sut.parse(phone, "")
        assertNotNull(parsed)
        assertEquals("+32474123456", parsed.number)
    }

    @Test
    fun formats_US_number_correctly() = runTest {
        val sut = PhoneValidator()
        val phone = "2125551234"
        val region = "US"
        assertEquals(true, sut.isValid(phone, region))
        val parsed = sut.parse(phone, region)
        assertNotNull(parsed)
        assertEquals("+12125551234", parsed.number)
    }

    @Test
    fun formats_UK_number_correctly() = runTest {
        val sut = PhoneValidator()
        val phone = "02071234567"
        val region = "GB"
        assertEquals(true, sut.isValid(phone, region))
        val parsed = sut.parse(phone, region)
        assertNotNull(parsed)
        assertEquals("+442071234567", parsed.number)
    }

    @Test
    fun preserves_correct_country_code_in_E164_format() = runTest {
        val sut = PhoneValidator()
        val phone = "0474 12 34 56"
        val region = "BE"
        val parsed = sut.parse(phone, region)
        assertNotNull(parsed)
        assertEquals("+32474123456", parsed.number)
    }

    @Test
    fun does_not_change_country_code_when_region_specified() = runTest {
        val sut = PhoneValidator()
        val phone = "0474 12 34 56"
        val wrongRegion = "US"
        val parsed = sut.parse(phone, wrongRegion)
        assertNotNull(parsed)
        assertEquals("+10474123456", parsed.number)
    }
}