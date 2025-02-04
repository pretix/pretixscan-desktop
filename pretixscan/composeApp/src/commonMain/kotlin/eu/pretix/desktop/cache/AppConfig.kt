package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.config.ConfigStore
import org.joda.time.DateTime
import org.json.JSONObject
import java.io.File
import java.util.prefs.Preferences


data class EventSelection(
    val eventSlug: String,
    val eventName: String,
    val subEventId: Long?,
    val checkInList: Long,
    val dateFrom: DateTime?,
    val dateTo: DateTime?,
)

class AppConfig(val dataDir: String) : ConfigStore {
    private val prefs = Preferences.userNodeForPackage(AppConfig::class.java)

    private val VERSION = Version.version

    private val PREFS_KEY_API_URL = "pretix_api_url"
    private val PREFS_KEY_API_KEY = "pretix_api_key"
    private val PREFS_KEY_API_DEVICE_ID = "pretix_api_device_id"
    private val PREFS_KEY_DEVICE_SERIAL = "device_pos_serial"
    private val PREFS_KEY_EVENT_SLUG = "pretix_api_event_slug"
    private val PREFS_KEY_EVENT_NAME = "pretix_api_event_name"
    private val PREFS_KEY_ORGANIZER_SLUG = "pretix_api_organizer_slug"
    private val PREFS_KEY_SUBEVENT_ID = "pretix_api_subevent_id"
    private val PREFS_KEY_CHECKINLIST_ID = "pretix_api_checkin_list_id"
    private val PREFS_KEY_CHECKINLIST_NAME = "pretix_api_checkin_list_name"
    private val PREFS_KEY_PRINTER_BADGE_NAME = "printer_badge_name"
    private val PREFS_KEY_PRINTER_BADGE_ORIENTATION = "printer_badge_orientation"
    private val PREFS_KEY_AUTO_PRINT_BADGES = "auto_print_badges"

    private val PREFS_KEY_PRINTBADGES = "prefs_key_print_badges"
    private val PREFS_KEY_SYNC_ORDERS = "sync_orders"
    private val PREFS_KEY_SHOW_INFO = "show_info"
    private val PREFS_KEY_DEVICE_KNOWN_VERSION = "known_version"
    private val PREFS_KEY_DEVICE_KNOWN_INFO = "known_info"
    private val PREFS_KEY_SCAN_TYPE = "scan_type"
    private val PREFS_KEY_PRETIX_KNOWN_VERSION = "known_pretix_version"
    private val PREFS_KEY_KNOWN_DEVICE_NAME = "known_device_name"
    private val PREFS_KEY_KNOWN_GATE_NAME = "known_gate_name"
    private val PREFS_KEY_KNOWN_GATE_ID = "known_gate_id"
    private val PREFS_KEY_PLAY_SOUND = "play_sound"
    private val PREFS_KEY_AUTO_SWITCH = "auto_switch"
    private val PREFS_KEY_LARGE_COLOR = "large_color"
    private val PREFS_KEY_ALLOW_SEARCH = "allow_search"
    private val PREFS_KEY_API_VERSION = "pretix_api_version"
    private val PREFS_KEY_ASYNC_MODE = "async"
    private val PREFS_KEY_PROXY_MODE = "proxy"
    private val PREFS_KEY_LAST_SYNC = "last_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC = "last_failed_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC_MSG = "last_failed_sync_msg"
    private val PREFS_KEY_LAST_DOWNLOAD = "last_download"
    private val PREFS_KEY_LAST_CLEANUP = "last_cleanup"
    private val PREFS_KEY_LAST_STATUS_DATA = "last_status_data"
    private val PREFS_KEY_LAST_UPDATE_CHECK = "last_update_check_"
    private val PREFS_KEY_UPDATE_CHECK_NEWER_VERSION = "update_check_newer_version_"
    private val PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS = "cache_known_live_event_slugs"

    private val PREFS_KEY_HIDE_NAMES = "pref_hide_names"

    fun setDeviceConfig(
        url: String,
        key: String,
        orga_slug: String,
        device_id: Long,
        serial: String,
        sent_version: Int
    ) {
        prefs.put(PREFS_KEY_API_URL, url)
        prefs.put(PREFS_KEY_API_KEY, key)
        prefs.put(PREFS_KEY_DEVICE_SERIAL, serial)
        prefs.putLong(PREFS_KEY_API_DEVICE_ID, device_id)
        prefs.put(PREFS_KEY_ORGANIZER_SLUG, orga_slug)
        prefs.putInt(PREFS_KEY_DEVICE_KNOWN_VERSION, sent_version)
        prefs.remove(PREFS_KEY_LAST_DOWNLOAD)
        prefs.remove(PREFS_KEY_LAST_CLEANUP)
        prefs.remove(PREFS_KEY_LAST_SYNC)
        prefs.remove(PREFS_KEY_LAST_FAILED_SYNC)
        prefs.remove(PREFS_KEY_LAST_STATUS_DATA)
        prefs.remove(PREFS_KEY_SUBEVENT_ID)
        prefs.remove(PREFS_KEY_EVENT_SLUG)
        prefs.remove(PREFS_KEY_EVENT_NAME)
        prefs.remove(PREFS_KEY_CHECKINLIST_ID)
        prefs.remove(PREFS_KEY_HIDE_NAMES)
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
        prefs.remove(PREFS_KEY_KNOWN_GATE_NAME)
        prefs.remove(PREFS_KEY_KNOWN_GATE_ID)
        prefs.remove(PREFS_KEY_HIDE_NAMES)
        val f = File(dataDir, PREFS_KEY_LAST_STATUS_DATA + ".json")
        if (f.exists()) {
            f.delete()
        }
        prefs.flush()
    }

    var proxyMode
        get() = prefs.getBoolean(PREFS_KEY_PROXY_MODE, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_PROXY_MODE, value)
            prefs.flush()
        }

    var asyncModeEnabled
        get() = prefs.getBoolean(PREFS_KEY_ASYNC_MODE, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_ASYNC_MODE, value)
            prefs.flush()
        }

    var eventSelection: List<EventSelection>
        get() {
            val slug = eventSlug
            val name = eventName

            if (slug == null || name == null) {
                return emptyList()
            }
            return listOf(
                EventSelection(
                    slug,
                    name,
                    subEventId ?: -1,
                    checkInListId,
                    null,
                    null
                )
            )
        }
        set(value) {
            //TODO: not implemented yet
        }

    fun eventSelectionToMap(): Map<String, Long> {
        return eventSelection.map { it.eventSlug to it.checkInList }.toMap()
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

    override fun getLastCleanup(): Long {
        return prefs.getLong(PREFS_KEY_LAST_CLEANUP, 0)
    }

    override fun setLastCleanup(`val`: Long) {
        prefs.putLong(PREFS_KEY_LAST_CLEANUP, `val`)
        prefs.flush()
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

    var eventSlug: String?
        get() = prefs.get(PREFS_KEY_EVENT_SLUG, null)
        set(value) {
            prefs.put(PREFS_KEY_EVENT_SLUG, value)
            prefs.flush()
        }

    override fun getSyncCycleId(): String {
        return "0"
    }

    override fun getSynchronizedEvents(): List<String> {
        val e = eventSlug
        if (e != null) return listOf(e)
        return emptyList()
    }

    override fun getSelectedSubeventForEvent(event: String): Long? {
        val se = subEventId ?: return null
        if (se < 1) return null
        return se
    }

    override fun getSelectedCheckinListForEvent(event: String?): Long? {
        return checkInListId
    }

    var subEventId: Long?
        get() {
            val l = prefs.getLong(PREFS_KEY_SUBEVENT_ID, 0L)
            if (l == 0L) {
                return null
            }
            return l
        }
        set(value) {
            prefs.putLong(PREFS_KEY_SUBEVENT_ID, value ?: 0L)
            prefs.flush()
        }

    fun setOrganizerSlug(value: String) {
        prefs.put(PREFS_KEY_ORGANIZER_SLUG, value)
        prefs.flush()
    }

    override fun getPosId(): Long {
        return 0;
    }

    var eventName: String?
        get() = prefs.get(PREFS_KEY_EVENT_NAME, null)
        set(value) {
            prefs.put(PREFS_KEY_EVENT_NAME, value)
            prefs.flush()
        }

    var playSound: Boolean
        get() = prefs.getBoolean(PREFS_KEY_PLAY_SOUND, true)
        set(value) {
            prefs.putBoolean(PREFS_KEY_PLAY_SOUND, value)
            prefs.flush()
        }

    var hideNames: Boolean
        get() = prefs.getBoolean(PREFS_KEY_HIDE_NAMES, true)
        set(value) {
            prefs.putBoolean(PREFS_KEY_HIDE_NAMES, value)
            prefs.flush()
        }

    var largeColor: Boolean
        get() = prefs.getBoolean(PREFS_KEY_LARGE_COLOR, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_LARGE_COLOR, value)
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
        set(value) {
            prefs.put(PREFS_KEY_UPDATE_CHECK_NEWER_VERSION + VERSION, value)
            prefs.flush()
        }

    var autoPrintBadges: Boolean
        get() = prefs.getBoolean(PREFS_KEY_AUTO_PRINT_BADGES, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_AUTO_PRINT_BADGES, value)
            prefs.flush()
        }

    var printBadges: Boolean
        get() = prefs.getBoolean(PREFS_KEY_PRINTBADGES, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_PRINTBADGES, value)
            prefs.flush()
        }

    var badgePrinterName: String?
        get() = prefs.get(PREFS_KEY_PRINTER_BADGE_NAME, null)
        set(value) {
            prefs.put(PREFS_KEY_PRINTER_BADGE_NAME, value)
            prefs.flush()
        }

    var badgePrinterOrientation: String
        get() = prefs.get(PREFS_KEY_PRINTER_BADGE_ORIENTATION, "Auto")
        set(value) {
            prefs.put(PREFS_KEY_PRINTER_BADGE_ORIENTATION, value)
            prefs.flush()
        }

    var scanType: String
        get() = prefs.get(PREFS_KEY_SCAN_TYPE, "entry")
        set(value) {
            prefs.put(PREFS_KEY_SCAN_TYPE, value)
            prefs.flush()
        }

    var syncOrders: Boolean
        get() = prefs.getBoolean(PREFS_KEY_SYNC_ORDERS, true)
        set(value) {
            prefs.putBoolean(PREFS_KEY_SYNC_ORDERS, value)
            prefs.flush()
        }

    override fun getDeviceKnownVersion(): Int {
        return prefs.getInt(PREFS_KEY_DEVICE_KNOWN_VERSION, 0)
    }

    override fun setDeviceKnownVersion(value: Int) {
        prefs.putInt(PREFS_KEY_DEVICE_KNOWN_VERSION, value)
        prefs.flush()
    }

    override fun getDeviceKnownInfo(): JSONObject {
        return JSONObject(prefs.get(PREFS_KEY_DEVICE_KNOWN_INFO, "{}") ?: "{}")
    }

    override fun setDeviceKnownInfo(value: JSONObject?) {
        prefs.put(PREFS_KEY_DEVICE_KNOWN_INFO, value?.toString() ?: "{}")
        prefs.flush()
    }

    override fun setKnownPretixVersion(value: Long) {
        return prefs.putLong(PREFS_KEY_PRETIX_KNOWN_VERSION, value)
    }

    override fun getKnownPretixVersion(): Long {
        return prefs.getLong(PREFS_KEY_PRETIX_KNOWN_VERSION, 0)
    }

    override fun getDeviceKnownName(): String {
        return prefs.get(PREFS_KEY_KNOWN_DEVICE_NAME, "")
    }

    override fun setDeviceKnownName(value: String?) {
        prefs.put(PREFS_KEY_KNOWN_DEVICE_NAME, value ?: "")
    }

    override fun getDeviceKnownGateName(): String {
        return prefs.get(PREFS_KEY_KNOWN_GATE_NAME, "")
    }

    override fun setDeviceKnownGateName(value: String?) {
        return prefs.put(PREFS_KEY_KNOWN_GATE_NAME, value ?: "")
    }

    override fun getDeviceKnownGateID(): Long {
        return prefs.getLong(PREFS_KEY_KNOWN_GATE_ID, 0L)
    }

    override fun setDeviceKnownGateID(value: Long?) {
        return prefs.putLong(PREFS_KEY_KNOWN_GATE_ID, value ?: 0)
    }

    override fun getAutoSwitchRequested(): Boolean {
        return prefs.getBoolean(PREFS_KEY_AUTO_SWITCH, false)
    }

    fun setAutoSwitchRequested(value: Boolean) {
        return prefs.putBoolean(PREFS_KEY_AUTO_SWITCH, value)
    }

    override fun getKnownLiveEventSlugs(): Set<String> {
        return prefs.get(PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS, "").split(",").filter { it.isNotEmpty() }.toSet()
    }

    override fun setKnownLiveEventSlugs(slugs: Set<String>) {
        prefs.put(PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS, slugs.joinToString(","))
        prefs.flush()
    }
}