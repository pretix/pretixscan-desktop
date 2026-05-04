package eu.pretix.scan.main.utils

import java.util.logging.Logger

private val logger = Logger.getLogger("DateFormatUtils")

fun org.joda.time.DateTime.isMidnight(): Boolean {
    return hourOfDay == 0 && minuteOfHour == 0 && secondOfMinute == 0 && millisOfSecond == 0
}

fun formatEventDateTime(dateTime: org.joda.time.DateTime?): String {
    if (dateTime == null) return ""

    return try {
        if (dateTime.isMidnight()) {
            org.joda.time.format.DateTimeFormat.mediumDate().print(dateTime)
        } else {
            org.joda.time.format.DateTimeFormat.mediumDateTime().print(dateTime)
        }
    } catch (e: Exception) {
        logger.warning("Failed to format date time: ${e.message}")
        dateTime.toString()
    }
}

/**
 * Formats an event date range with optional end date.
 * Returns the formatted start date, or a range if an end date is provided.
 */
fun formatEventDateTimeRange(from: org.joda.time.DateTime?, to: org.joda.time.DateTime?): String {
    if (from == null) return ""

    val fromFormatted = formatEventDateTime(from)
    if (to == null) return fromFormatted

    val toFormatted = formatEventDateTime(to)
    return if (toFormatted.isNotEmpty()) {
        "$fromFormatted - $toFormatted"
    } else {
        fromFormatted
    }
}
