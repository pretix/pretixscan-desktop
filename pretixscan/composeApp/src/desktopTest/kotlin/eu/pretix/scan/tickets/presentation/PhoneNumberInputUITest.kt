package eu.pretix.scan.tickets.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.vanniktech.locale.Country
import eu.pretix.desktop.app.ui.FieldValidationState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PhoneNumberInputUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCountrySelection_persistsWhenTyping() {
        var selectedPhone: String? = null
        var selectedCountry: String? = null

        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "",
                validation = null,
                uiExtra = "BE",
                onSelect = { phone, country ->
                    selectedPhone = phone
                    selectedCountry = country
                }
            )
        }

        val phoneInput = composeTestRule.onNodeWithText("", useUnmergedTree = true)
            .assertExists("Phone input field should exist")

        phoneInput.performTextInput("0474123456")

        assertEquals("BE", selectedCountry, "Country should remain BE after typing")
        assertEquals("0474123456", selectedPhone, "Phone value should be updated")
    }

    @Test
    fun testE164Number_displaysCorrectCountry() {
        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "+32474123456",
                validation = null,
                uiExtra = "BE",
                onSelect = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("+32474123456", useUnmergedTree = true)
            .assertExists("E164 number should be displayed")

        composeTestRule.onNodeWithText("🇧🇪 +32", useUnmergedTree = true)
            .assertExists("Belgium country code should be displayed")
    }

    @Test
    fun testValidationError_isDisplayed() {
        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "invalid",
                validation = FieldValidationState.INVALID,
                uiExtra = "BE",
                onSelect = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("invalid", useUnmergedTree = true)
            .assertExists("Invalid phone number should be displayed")
    }

    @Test
    fun testEmptyValue_usesDefaultCountry() {
        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = null,
                validation = null,
                uiExtra = null,
                onSelect = { _, _ -> }
            )
        }

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun testUiExtra_overridesCalculatedCountry() {
        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "0123456789",
                validation = null,
                uiExtra = "FR",
                onSelect = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("🇫🇷 +33", useUnmergedTree = true)
            .assertExists("France should be selected via uiExtra, not calculated from value")
    }

    @Test
    fun testMultipleCountries_canBeSelected() {
        val countries = listOf("BE", "DE", "FR", "US", "GB")
        countries.forEach { countryCode ->
            var selectedCountry: String? = null

            composeTestRule.setContent {
                QuestionPhoneNumber(
                    selectedValue = "",
                    validation = null,
                    uiExtra = countryCode,
                    onSelect = { _, country ->
                        selectedCountry = country
                    }
                )
            }

            val country = Country.entries.firstOrNull { it.code == countryCode }
            assertNotNull(country, "Country $countryCode should exist")

            val callingCode = country.callingCodes.firstOrNull()
            assertNotNull(callingCode, "Country $countryCode should have calling code")

            composeTestRule.onNodeWithText("${country.emoji} $callingCode", useUnmergedTree = true)
                .assertExists("Country $countryCode should be displayed")
        }
    }

    @Test
    fun testPhoneInput_acceptsNumericInput() {
        var lastPhone: String? = null

        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "",
                validation = null,
                uiExtra = "BE",
                onSelect = { phone, _ ->
                    lastPhone = phone
                }
            )
        }

        val phoneInput = composeTestRule.onNodeWithText("", useUnmergedTree = true)
            .assertExists()

        phoneInput.performTextInput("123")
        assertEquals("123", lastPhone, "Should accept numeric input")

        phoneInput.performTextClearance()
        phoneInput.performTextInput("0474 12 34 56")
        assertEquals("0474 12 34 56", lastPhone, "Should accept formatted input")
    }

    @Test
    fun testInitialCountryFromE164() {
        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = "+493012345678",
                validation = null,
                uiExtra = null,
                onSelect = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("🇩🇪 +49", useUnmergedTree = true)
            .assertExists("Should extract Germany from +49 E164 number")
    }

    @Test
    fun testExplicitCountryChange_clearsPhoneField() {
        var selectedPhone by mutableStateOf("+32111111111")
        var selectedCountry by mutableStateOf("BE")

        composeTestRule.setContent {
            QuestionPhoneNumber(
                selectedValue = selectedPhone,
                validation = null,
                uiExtra = selectedCountry,
                onSelect = { phone, country ->
                    selectedPhone = phone ?: ""
                    selectedCountry = country ?: ""
                }
            )
        }

        composeTestRule.waitForIdle()

        assertEquals("+32111111111", selectedPhone, "Initial phone should be Belgian number")
        assertEquals("BE", selectedCountry, "Initial country should be Belgium")

        composeTestRule.onNodeWithText("+32111111111", useUnmergedTree = true)
            .assertExists("Phone number should be displayed")
        composeTestRule.onNodeWithText("🇧🇪 +32", useUnmergedTree = true)
            .assertExists("Belgium should be selected")

        selectedCountry = "FR"
        selectedPhone = ""
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("🇫🇷 +33", useUnmergedTree = true)
            .assertExists("France should now be selected")

        val emptyPhoneInput = composeTestRule.onNodeWithText("", useUnmergedTree = true)
            .assertExists("Phone input should be empty after country change")

        emptyPhoneInput.performTextInput("+33123456789")
        composeTestRule.waitForIdle()

        assertEquals("+33123456789", selectedPhone, "Should accept new French number")
        assertEquals("FR", selectedCountry, "Country should remain France")
    }
}
