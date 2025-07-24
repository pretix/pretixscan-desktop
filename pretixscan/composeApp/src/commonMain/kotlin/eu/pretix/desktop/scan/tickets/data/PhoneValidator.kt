@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package eu.pretix.desktop.scan.tickets.data

data class PretixPhoneNumber(
    val rawValue: String,
    val number: String
)

expect class PhoneValidator() {
    fun parse(number: String, regionCode: String?): PretixPhoneNumber?

    fun isValid(number: String, regionCode: String?): Boolean
}


