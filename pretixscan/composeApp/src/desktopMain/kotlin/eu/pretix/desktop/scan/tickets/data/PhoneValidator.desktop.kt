@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package eu.pretix.desktop.scan.tickets.data

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil


actual class PhoneValidator {
    actual fun parse(number: String, regionCode: String?): PretixPhoneNumber? {
        val region = regionCode ?: ""
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = phoneUtil.parseAndKeepRawInput(number, region)
            val formattedNumber = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            return PretixPhoneNumber(
                rawValue = phoneNumber.rawInput,
                number = formattedNumber
            )
        } catch (e: NumberParseException) {
            e.printStackTrace()
            return null
        }
    }

    actual fun isValid(number: String, regionCode: String?): Boolean {
        val phoneNumber = parse(number, regionCode)
        return phoneNumber != null
    }
}