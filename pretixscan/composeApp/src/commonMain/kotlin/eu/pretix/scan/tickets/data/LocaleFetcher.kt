package eu.pretix.scan.tickets.data

import com.vanniktech.locale.Country
import java.text.SimpleDateFormat
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

/**
 * Returns the system's default locale
 */
fun getSystemLocale(): Locale = Locale.getDefault()

/**
 * Creates a locale-aware SimpleDateFormat for date formatting (yyyy-MM-dd)
 * This format is used for data exchange/storage and should remain consistent
 */
fun getDateFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
}

/**
 * Creates a locale-aware SimpleDateFormat for time formatting (HH:mm)
 * This format is used for data exchange/storage and should remain consistent
 */
fun getTimeFormat(): SimpleDateFormat {
    return SimpleDateFormat("HH:mm", Locale.US)
}

/**
 * Creates a locale-aware SimpleDateFormat for datetime formatting (yyyy-MM-dd'T'HH:mm)
 * This format is used for data exchange/storage and should remain consistent
 */
fun getDateTimeFormat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
}

/**
 * Creates a locale-aware SimpleDateFormat for displaying dates to the user
 */
fun getDisplayDateFormat(): SimpleDateFormat {
    val locale = getSystemLocale()
    return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale) as SimpleDateFormat
}

/**
 * Creates a locale-aware SimpleDateFormat for displaying times to the user
 */
fun getDisplayTimeFormat(): SimpleDateFormat {
    val locale = getSystemLocale()
    return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale) as SimpleDateFormat
}