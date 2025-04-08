package eu.pretix.desktop.app.sync

import androidx.compose.ui.graphics.Color
import eu.pretix.desktop.app.ui.CustomColor
import eu.pretix.desktop.app.ui.asColor
import eu.pretix.desktop.cache.AppConfig
import java.util.*

class SyncStatusHelper(private val appConfig: AppConfig) {

    fun getColor(): Color {
        if (appConfig.lastFailedSync > appConfig.lastSync || System.currentTimeMillis() - appConfig.lastDownload > 5 * 60 * 1000) {
            return CustomColor.BrandRed.asColor()
        }

        return CustomColor.BrandGreen.asColor()
    }

    fun sinceLastDownload(): Long {
        return Date().time - appConfig.lastDownload
    }

    fun isNever(): Boolean {
        // it appears "never" sometimes returns as 20186
        return sinceLastDownload() == 0L || (sinceLastDownload() / (24 * 3600 * 1000)).toInt() == 20186
    }

    fun isDaysAgo(): Boolean {
        return sinceLastDownload() > 24 * 3600 * 1000
    }

    fun daysAgo(): Int {
        return (sinceLastDownload() / (24 * 3600 * 1000)).toInt()
    }

    fun isHoursAgo(): Boolean {
        return sinceLastDownload() > 3600 * 1000
    }

    fun hoursAgo(): Int {
        return (sinceLastDownload() / (3600 * 1000)).toInt()
    }

    fun isMinutesAgo(): Boolean {
        return sinceLastDownload() > 60 * 1000
    }

    fun minutesAgo(): Int {
        return (sinceLastDownload() / (60 * 1000)).toInt()
    }
}