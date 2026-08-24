package eu.pretix.scan.tickets.presentation

import com.vanniktech.locale.Country
import kotlin.test.Test
import kotlin.test.assertEquals

class CountryReadableNameTest {

    @Test
    fun `USA acronym should display as USA not Usa`() {
        val country = Country.entries.find { it.code == "US" }!!
        val result = country.readableName()
        assertEquals("USA", result)
    }

    @Test
    fun `BELGIUM should display as Belgium`() {
        val country = Country.entries.find { it.code == "BE" }!!
        assertEquals("Belgium", country.readableName())
    }

    @Test
    fun `GERMANY should display as Germany`() {
        val country = Country.entries.find { it.code == "DE" }!!
        assertEquals("Germany", country.readableName())
    }

    @Test
    fun `multi word country should have proper title case`() {
        val country = Country.entries.find { it.name.contains("_") }!!
        val result = country.readableName()
        val words = result.split(" ")
        words.forEach { word ->
            assertEquals(word[0].uppercaseChar(), word[0], "First letter should be uppercase in: $word")
        }
    }
}
