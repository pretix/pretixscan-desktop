package eu.pretix.desktop.cache

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.config.ConfigStore
import eu.pretix.pretixscan.desktop.PretixScanConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import org.json.JSONObject
import java.io.File
import java.util.logging.Logger
import java.util.prefs.Preferences


object DateTimeSerializer : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeLong(value.millis)
    }

    override fun deserialize(decoder: Decoder): DateTime {
        return DateTime(decoder.decodeLong())
    }
}

@Suppress("PrivatePropertyName")
class AppConfig(val dataDir: String) : ConfigStore {
    private val prefs = Preferences.userNodeForPackage(PretixScanConfig::class.java)
    private val log = Logger.getLogger(AppConfig::class.java.name)

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

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

    private val PREFS_KEY_PREFERRED_CAMERA_NAME = "preferred_camera_name"
    private val PREFS_KEY_AUTO_PRINT_BADGES = "auto_print_badges"

    private val PREFS_KEY_PRINTBADGES = "prefs_key_print_badges"
    private val PREFS_KEY_SYNC_ORDERS = "sync_orders"
    private val PREFS_KEY_SYNC_AUTO = "pref_sync_auto"
    private val PREFS_KEY_DEVICE_KNOWN_VERSION = "known_version"
    private val PREFS_KEY_DEVICE_KNOWN_INFO = "known_info"
    private val PREFS_KEY_SCAN_TYPE = "scan_type"
    private val PREFS_KEY_PRETIX_KNOWN_VERSION = "known_pretix_version"
    private val PREFS_KEY_KNOWN_DEVICE_NAME = "known_device_name"
    private val PREFS_KEY_KNOWN_GATE_NAME = "known_gate_name"
    private val PREFS_KEY_KNOWN_GATE_ID = "known_gate_id"
    private val PREFS_KEY_PLAY_SOUND = "play_sound"
    private val PREFS_KEY_AUTO_SWITCH = "auto_switch"
    private val PREFS_KEY_API_VERSION = "pretix_api_version"
    private val PREFS_KEY_OFFLINE_MODE = "offline"
    private val PREFS_KEY_PROXY_MODE = "proxy"
    private val PREFS_KEY_LAST_SYNC = "last_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC = "last_failed_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC_MSG = "last_failed_sync_msg"
    private val PREFS_KEY_LAST_DOWNLOAD = "last_download"
    private val PREFS_KEY_LAST_CLEANUP = "last_cleanup"
    private val PREFS_KEY_UI_REDUCE_MOTION = "ui_reduce_motion"
    private val PREFS_KEY_LAST_STATUS_DATA = "last_status_data"
    private val PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS = "cache_known_live_event_slugs"

    private val PREFS_KEY_HIDE_NAMES = "pref_hide_names"

    private val PREFS_KEY_EVENT_SELECTIONS = "event_selections_json"
    private val PREFS_KEY_ACTIVE_EVENT_INDEX = "active_event_index"

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

    var proxyMode
        get() = prefs.getBoolean(PREFS_KEY_PROXY_MODE, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_PROXY_MODE, value)
            prefs.flush()
        }

    var offlineMode
        get() = prefs.getBoolean(PREFS_KEY_OFFLINE_MODE, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_OFFLINE_MODE, value)
            prefs.flush()
        }

    var eventSelections: List<EventSelection>
        get() {
            val jsonString = prefs.get(PREFS_KEY_EVENT_SELECTIONS, null)
            if (jsonString == null) {
                // Migration: convert old single event to list
                val slug = eventSlug
                val name = eventName
                val listId = checkInListId
                val listName = checkInListName

                if (slug != null && name != null && listId > 0) {
                    return listOf(
                        EventSelection(
                            eventSlug = slug,
                            eventName = name,
                            subEventId = subEventId,
                            checkInListId = listId,
                            checkInListName = listName,
                            dateFrom = null,
                            dateTo = null
                        )
                    )
                }
                return emptyList()
            }

            return try {
                json.decodeFromString<List<EventSelection>>(jsonString)
            } catch (e: Exception) {
                log.warning("Failed to parse event selections: ${e.message}")
                emptyList()
            }
        }
        set(value) {
            val jsonString = json.encodeToString(value)
            prefs.put(PREFS_KEY_EVENT_SELECTIONS, jsonString)
            prefs.flush()
        }

    var activeEventIndex: Int
        get() = prefs.getInt(PREFS_KEY_ACTIVE_EVENT_INDEX, 0)
        set(value) {
            prefs.putInt(PREFS_KEY_ACTIVE_EVENT_INDEX, value)
            prefs.flush()
        }

    val activeEvent: EventSelection?
        get() = eventSelections.getOrNull(activeEventIndex)

    @Deprecated("Use eventSelections (plural) instead", ReplaceWith("eventSelections"))
    var eventSelection: List<EventSelection>
        get() = eventSelections
        set(value) {
            eventSelections = value
        }

    fun eventSelectionToMap(): Map<String, Long> {
        return eventSelections.associate { it.eventSlug to it.checkInListId }
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
        return eventSelections.map { it.eventSlug }
    }

    override fun getSelectedSubeventForEvent(event: String): Long? {
        return eventSelections.find { it.eventSlug == event }?.subEventId
    }

    override fun getSelectedCheckinListForEvent(event: String?): Long {
        if (event == null) return activeEvent?.checkInListId ?: 0
        return eventSelections.find { it.eventSlug == event }?.checkInListId ?: 0
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

    override fun getPosId(): Long {
        return 0
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

    var uiHideNames: Boolean
        get() = prefs.getBoolean(PREFS_KEY_HIDE_NAMES, true)
        set(value) {
            prefs.putBoolean(PREFS_KEY_HIDE_NAMES, value)
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

    var autoPrintBadges: Boolean
        get() = prefs.getBoolean(PREFS_KEY_AUTO_PRINT_BADGES, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_AUTO_PRINT_BADGES, value)
            prefs.flush()
        }

    var uiReduceMotion: Boolean
        get() = prefs.getBoolean(PREFS_KEY_UI_REDUCE_MOTION, false)
        set(value) {
            prefs.putBoolean(PREFS_KEY_UI_REDUCE_MOTION, value)
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

    var preferredCameraName: String
        get() = prefs.get(PREFS_KEY_PREFERRED_CAMERA_NAME, "-")
        set(value) {
            prefs.put(PREFS_KEY_PREFERRED_CAMERA_NAME, value)
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

    var syncAuto: Boolean
        get() = prefs.getBoolean(PREFS_KEY_SYNC_AUTO, true)
        set(value) {
            prefs.putBoolean(PREFS_KEY_SYNC_AUTO, value)
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

    override fun getKnownLiveEventSlugs(): Set<String> {
        return prefs.get(PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS, "").split(",").filter { it.isNotEmpty() }.toSet()
    }

    override fun setKnownLiveEventSlugs(slugs: Set<String>) {
        prefs.put(PREFS_KEY_KNOWN_LIVE_EVENT_SLUGS, slugs.joinToString(","))
        prefs.flush()
    }

    internal fun getApiDeviceId(): Long {
        return prefs.getLong(PREFS_KEY_API_DEVICE_ID, 0L)
    }

    internal val deviceSerial: String
        get() = prefs.get(PREFS_KEY_DEVICE_SERIAL, "")


    fun resetEventConfig() {
        prefs.remove(PREFS_KEY_API_URL)
        prefs.remove(PREFS_KEY_API_KEY)
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
        val f = File(dataDir, "$PREFS_KEY_LAST_STATUS_DATA.json")
        if (f.exists()) {
            f.delete()
        }
        prefs.flush()
    }
}