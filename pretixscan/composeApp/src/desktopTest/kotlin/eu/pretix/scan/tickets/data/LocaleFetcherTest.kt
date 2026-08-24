package eu.pretix.scan.tickets.data

import com.vanniktech.locale.Country
import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleFetcherTest {

    @Test
    fun `calculateDefaultCountry with +32 prefix returns Belgium`() {
        val result = calculateDefaultCountry("+32474123456")
        val expectedCountry = Country.entries.find { it.code == "BE" }!!
        assertEquals(expectedCountry, result)
    }

    @Test
    fun `calculateDefaultCountry with +49 prefix returns Germany`() {
        val result = calculateDefaultCountry("+493012345678")
        val expectedCountry = Country.entries.find { it.code == "DE" }!!
        assertEquals(expectedCountry, result)
    }

    @Test
    fun `calculateDefaultCountry with +1 prefix returns country with +1 calling code`() {
        val result = calculateDefaultCountry("+12125551234")
        val hasCallingCodeOne = result.callingCodes.any { it == "+1" }
        assertEquals(true, hasCallingCodeOne, "Expected country with +1 calling code but got ${result.code}")
    }

    @Test
    fun `calculateDefaultCountry with +44 prefix returns GB`() {
        val result = calculateDefaultCountry("+442071234567")
        val expectedCountry = Country.entries.find { it.code == "GB" }!!
        assertEquals(expectedCountry, result)
    }

    @Test
    fun `calculateDefaultCountry with local number returns locale default`() {
        val result = calculateDefaultCountry("0474123456")
        assertEquals(Country.fromOrNull(java.util.Locale.getDefault().country) ?: Country.ENGLAND, result)
    }

    @Test
    fun `calculateDefaultCountry with empty string returns locale default`() {
        val result = calculateDefaultCountry("")
        assertEquals(Country.fromOrNull(java.util.Locale.getDefault().country) ?: Country.ENGLAND, result)
    }

    @Test
    fun `calculateDefaultCountry with null returns locale default`() {
        val result = calculateDefaultCountry(null)
        assertEquals(Country.fromOrNull(java.util.Locale.getDefault().country) ?: Country.ENGLAND, result)
    }

    @Test
    fun `calculateDefaultCountry with blank string returns locale default`() {
        val result = calculateDefaultCountry("   ")
        assertEquals(Country.fromOrNull(java.util.Locale.getDefault().country) ?: Country.ENGLAND, result)
    }
}
