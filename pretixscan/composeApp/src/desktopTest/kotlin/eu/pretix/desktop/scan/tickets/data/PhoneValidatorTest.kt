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
}