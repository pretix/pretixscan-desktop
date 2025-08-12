package eu.pretix.scan.tickets.data

import com.vanniktech.locale.Country
import java.util.*

fun calculateDefaultCountry(value: String?): Country {
    val currentLocale: Locale = Locale.getDefault()
    val userCountry = Country.fromOrNull(currentLocale.country) ?: Country.ENGLAND
    if (value.isNullOrBlank()) {
        return userCountry
    }
    // guess the country from the calling code
    val matchingCountry = Country.entries.firstOrNull { c ->
        c.callingCodes.any { code ->
            value.startsWith(code)
        }
    }
    return matchingCountry ?: userCountry
}