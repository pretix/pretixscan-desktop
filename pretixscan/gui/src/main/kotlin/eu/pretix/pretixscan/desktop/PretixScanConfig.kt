package eu.pretix.pretixscan.desktop

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.config.ConfigStore
import java.io.File
import java.util.prefs.Preferences


class PretixScanConfig(private var data_dir: String) : ConfigStore {

    private val prefs = Preferences.userNodeForPackage(PretixScanConfig::class.java)

    private val PREFS_KEY_API_URL = "pretix_api_url"
    private val PREFS_KEY_API_KEY = "pretix_api_key"
    private val PREFS_KEY_API_DEVICE_ID = "pretix_api_device_id"
    private val PREFS_KEY_EVENT_SLUG = "pretix_api_event_slug"
    private val PREFS_KEY_EVENT_NAME = "pretix_api_event_name"
    private val PREFS_KEY_ORGANIZER_SLUG = "pretix_api_organizer_slug"
    private val PREFS_KEY_SUBEVENT_ID = "pretix_api_subevent_id"
    private val PREFS_KEY_CHECKINLIST_ID = "pretix_api_checkin_list_id"
    private val PREFS_KEY_CHECKINLIST_NAME = "pretix_api_checkin_list_name"
    private val PREFS_KEY_PRINTER_BADGE_NAME = "printer_badge_name"
    private val PREFS_KEY_AUTO_PRINT_BADGES = "auto_print_badges"
    private val PREFS_KEY_SHOW_INFO = "show_info"
    private val PREFS_KEY_DEVICE_KNOWN_VERSION = "known_version"
    private val PREFS_KEY_PLAY_SOUND = "play_sound"
    private val PREFS_KEY_ALLOW_SEARCH = "allow_search"
    private val PREFS_KEY_API_VERSION = "pretix_api_version"
    private val PREFS_KEY_ASYNC_MODE = "async"
    private val PREFS_KEY_LAST_SYNC = "last_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC = "last_failed_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC_MSG = "last_failed_sync_msg"
    private val PREFS_KEY_LAST_DOWNLOAD = "last_download"
    private val PREFS_KEY_LAST_STATUS_DATA = "last_status_data"
    private val PREFS_KEY_LAST_UPDATE_CHECK = "last_update_check_"
    private val PREFS_KEY_UPDATE_CHECK_NEWER_VERSION = "update_check_newer_version_"

    fun setDeviceConfig(url: String, key: String, orga_slug: String, device_id: Long, serial: String, sent_version: Int) {
        prefs.put(PREFS_KEY_API_URL, url)
        prefs.put(PREFS_KEY_API_KEY, key)
        prefs.putLong(PREFS_KEY_API_DEVICE_ID, device_id)
        prefs.put(PREFS_KEY_ORGANIZER_SLUG, orga_slug)
        prefs.putInt(PREFS_KEY_DEVICE_KNOWN_VERSION, sent_version)
        prefs.remove(PREFS_KEY_LAST_DOWNLOAD)
        prefs.remove(PREFS_KEY_LAST_SYNC)
        prefs.remove(PREFS_KEY_LAST_FAILED_SYNC)
        prefs.remove(PREFS_KEY_LAST_STATUS_DATA)
        prefs.remove(PREFS_KEY_SUBEVENT_ID)
        prefs.remove(PREFS_KEY_EVENT_SLUG)
        prefs.remove(PREFS_KEY_EVENT_NAME)
        prefs.remove(PREFS_KEY_CHECKINLIST_ID)
        prefs.flush()
    }

    fun resetEventConfig() {
        prefs.remove(PREFS_KEY_API_URL)
        prefs.remove(PREFS_KEY_API_KEY)
        prefs.remove(PREFS_KEY_SHOW_INFO)
        prefs.remove(PREFS_KEY_ALLOW_SEARCH)
        prefs.remove(PREFS_KEY_API_VERSION)
        prefs.remove(PREFS_KEY_LAST_DOWNLOAD)
        prefs.remove(PREFS_KEY_LAST_SYNC)
        prefs.remove(PREFS_KEY_LAST_FAILED_SYNC)
        prefs.remove(PREFS_KEY_LAST_STATUS_DATA)
        prefs.remove(PREFS_KEY_SUBEVENT_ID)
        prefs.remove(PREFS_KEY_EVENT_SLUG)
        prefs.remove(PREFS_KEY_EVENT_NAME)
        prefs.remove(PREFS_KEY_CHECKINLIST_ID)
        val f = File(data_dir, PREFS_KEY_LAST_STATUS_DATA + ".json")
        if (f.exists()) {
            f.delete()
        }
        prefs.flush()
    }

    var asyncModeEnabled
        get() = prefs.getBoolean(PREFS_KEY_ASYNC_MODE, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_ASYNC_MODE, value)
            prefs.flush()
        }

    override fun isDebug(): Boolean {
        return false
    }

    override fun isConfigured(): Boolean {
        return prefs.get(PREFS_KEY_API_URL, null) != null && apiUrl.isNotEmpty()
    }

    override fun getApiVersion(): Int {
        return prefs.getInt(PREFS_KEY_API_VERSION, PretixApi.SUPPORTED_API_VERSION)
    }

    override fun getApiUrl(): String {
        return prefs.get(PREFS_KEY_API_URL, "")
    }

    override fun getApiKey(): String {
        return prefs.get(PREFS_KEY_API_KEY, "")
    }

    override fun getLastDownload(): Long {
        return prefs.getLong(PREFS_KEY_LAST_DOWNLOAD, 0)
    }

    override fun setLastDownload(value: Long) {
        prefs.putLong(PREFS_KEY_LAST_DOWNLOAD, value)
        prefs.flush()
    }

    override fun getLastSync(): Long {
        return prefs.getLong(PREFS_KEY_LAST_SYNC, 0)
    }

    override fun setLastSync(value: Long) {
        prefs.putLong(PREFS_KEY_LAST_SYNC, value)
        prefs.flush()
    }

    override fun getLastFailedSync(): Long {
        return prefs.getLong(PREFS_KEY_LAST_FAILED_SYNC, 0)
    }

    override fun setLastFailedSync(value: Long) {
        prefs.putLong(PREFS_KEY_LAST_FAILED_SYNC, value)
        prefs.flush()
    }

    override fun getLastFailedSyncMsg(): String {
        return prefs.get(PREFS_KEY_LAST_FAILED_SYNC_MSG, "")
    }

    override fun setLastFailedSyncMsg(value: String?) {
        prefs.put(PREFS_KEY_LAST_FAILED_SYNC_MSG, value)
        prefs.flush()
    }

    override fun getOrganizerSlug(): String {
        return prefs.get(PREFS_KEY_ORGANIZER_SLUG, "")
    }

    override fun getEventSlug(): String? {
        return prefs.get(PREFS_KEY_EVENT_SLUG, null)
    }

    override fun getSubEventId(): Long? {
        val l = prefs.getLong(PREFS_KEY_SUBEVENT_ID, 0L)
        if (l == 0L) {
            return null
        }
        return l
    }

    fun setOrganizerSlug(value: String) {
        prefs.put(PREFS_KEY_ORGANIZER_SLUG, value)
        prefs.flush()
    }

    fun setEventSlug(value: String?) {
        prefs.put(PREFS_KEY_EVENT_SLUG, value)
        prefs.flush()
    }

    fun setSubEventId(value: Long?) {
        prefs.putLong(PREFS_KEY_SUBEVENT_ID, value ?: 0L)
        prefs.flush()
    }

    override fun getPosId(): Long {
        return 0;
    }

    var eventName: String?
        get() = prefs.get(PREFS_KEY_EVENT_NAME, null)
        set (value) {
            prefs.put(PREFS_KEY_EVENT_NAME, value)
            prefs.flush()
        }

    var playSound: Boolean
        get() = prefs.getBoolean(PREFS_KEY_PLAY_SOUND, true)
        set (value) {
            prefs.putBoolean(PREFS_KEY_PLAY_SOUND, value)
            prefs.flush()
        }

    var checkInListId: Long
        get() = prefs.getLong(PREFS_KEY_CHECKINLIST_ID, 0)
        set(value) {
            prefs.putLong(PREFS_KEY_CHECKINLIST_ID, value)
            prefs.flush()
        }

    var checkInListName: String
        get() = prefs.get(PREFS_KEY_CHECKINLIST_NAME, "")
        set(value) {
            prefs.put(PREFS_KEY_CHECKINLIST_NAME, value)
            prefs.flush()
        }

    var lastUpdateCheck: Long
        get() = prefs.getLong(PREFS_KEY_LAST_UPDATE_CHECK + VERSION, 0)
        set(value) {
            prefs.putLong(PREFS_KEY_LAST_UPDATE_CHECK + VERSION, value)
            prefs.flush()
        }

    var updateCheckNewerVersion: String
        get() = prefs.get(PREFS_KEY_UPDATE_CHECK_NEWER_VERSION + VERSION, "")
        set (value) {
            prefs.put(PREFS_KEY_UPDATE_CHECK_NEWER_VERSION + VERSION, value)
            prefs.flush()
        }

    var autoPrintBadges: Boolean
        get() = prefs.getBoolean(PREFS_KEY_AUTO_PRINT_BADGES, false)
        set (value) {
            prefs.putBoolean(PREFS_KEY_AUTO_PRINT_BADGES, value)
            prefs.flush()
        }

    var badgePrinterName: String?
        get() = prefs.get(PREFS_KEY_PRINTER_BADGE_NAME, null)
        set (value) {
            prefs.put(PREFS_KEY_PRINTER_BADGE_NAME, value)
            prefs.flush()
        }
}