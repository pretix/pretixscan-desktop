package eu.pretix.desktop.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import eu.pretix.libpretixsync.api.PretixApi
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.json.JSONObject

fun createDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

class DataStoreConfig(private val dataStore: DataStore<Preferences>) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private object PreferenceKeys {
        // Migration tracking
        val MIGRATION_COMPLETE = booleanPreferencesKey("migration_complete_v1")

        // Device & API Configuration
        val API_URL = stringPreferencesKey("api_url")
        val API_KEY = stringPreferencesKey("api_key")
        val API_VERSION = intPreferencesKey("api_version")
        val API_DEVICE_ID = longPreferencesKey("api_device_id")
        val DEVICE_SERIAL = stringPreferencesKey("device_serial")
        val ORGANIZER_SLUG = stringPreferencesKey("organizer_slug")

        // Device Metadata
        val DEVICE_KNOWN_VERSION = intPreferencesKey("device_known_version")
        val DEVICE_KNOWN_INFO = stringPreferencesKey("device_known_info_json")
        val DEVICE_KNOWN_NAME = stringPreferencesKey("device_known_name")
        val DEVICE_KNOWN_GATE_NAME = stringPreferencesKey("device_known_gate_name")
        val DEVICE_KNOWN_GATE_ID = longPreferencesKey("device_known_gate_id")
        val PRETIX_KNOWN_VERSION = longPreferencesKey("pretix_known_version")

        // Event Configuration
        val EVENT_SELECTIONS = stringPreferencesKey("event_selections_json")
        val ACTIVE_EVENT_INDEX = intPreferencesKey("active_event_index")

        // Sync State
        val LAST_SYNC = longPreferencesKey("last_sync")
        val LAST_DOWNLOAD = longPreferencesKey("last_download")
        val LAST_CLEANUP = longPreferencesKey("last_cleanup")
        val LAST_FAILED_SYNC = longPreferencesKey("last_failed_sync")
        val LAST_FAILED_SYNC_MSG = stringPreferencesKey("last_failed_sync_msg")
        val KNOWN_LIVE_EVENT_SLUGS = stringPreferencesKey("known_live_event_slugs_json")

        // Mode Flags
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val PROXY_MODE = booleanPreferencesKey("proxy_mode")

        // UI Preferences
        val PLAY_SOUND = booleanPreferencesKey("play_sound")
        val UI_HIDE_NAMES = booleanPreferencesKey("ui_hide_names")
        val UI_REDUCE_MOTION = booleanPreferencesKey("ui_reduce_motion")
        val AUTO_PRINT_BADGES = booleanPreferencesKey("auto_print_badges")

        // Hardware Settings
        val PREFERRED_CAMERA_NAME = stringPreferencesKey("preferred_camera_name")
        val SCAN_TYPE = stringPreferencesKey("scan_type")
        val BADGE_PRINTER_NAME = stringPreferencesKey("badge_printer_name")
        val BADGE_PRINTER_ORIENTATION = stringPreferencesKey("badge_printer_orientation")

        // Feature Flags
        val PRINT_BADGES = booleanPreferencesKey("print_badges")
        val SYNC_ORDERS = booleanPreferencesKey("sync_orders")
        val SYNC_AUTO = booleanPreferencesKey("sync_auto")

        // Legacy single-event storage (coexists with eventSelections for backward compatibility)
        val EVENT_SLUG = stringPreferencesKey("event_slug")
        val SUB_EVENT_ID = longPreferencesKey("sub_event_id")
        val EVENT_NAME = stringPreferencesKey("event_name")
        val CHECKIN_LIST_ID = longPreferencesKey("checkin_list_id")
        val CHECKIN_LIST_NAME = stringPreferencesKey("checkin_list_name")
    }

    // ============================================================
    // Migration Status
    // ============================================================

    suspend fun isMigrationComplete(): Boolean =
        dataStore.data.first()[PreferenceKeys.MIGRATION_COMPLETE] ?: false

    suspend fun markMigrationComplete() {
        dataStore.edit { it[PreferenceKeys.MIGRATION_COMPLETE] = true }
    }

    // ============================================================
    // Device & API Configuration
    // ============================================================

    suspend fun getApiUrl(): String =
        dataStore.data.first()[PreferenceKeys.API_URL] ?: ""

    suspend fun setApiUrl(value: String) {
        dataStore.edit { it[PreferenceKeys.API_URL] = value }
    }

    suspend fun getApiKey(): String =
        dataStore.data.first()[PreferenceKeys.API_KEY] ?: ""

    suspend fun setApiKey(value: String) {
        dataStore.edit { it[PreferenceKeys.API_KEY] = value }
    }

    suspend fun getApiVersion(): Int =
        dataStore.data.first()[PreferenceKeys.API_VERSION] ?: PretixApi.SUPPORTED_API_VERSION

    suspend fun setApiVersion(value: Int) {
        dataStore.edit { it[PreferenceKeys.API_VERSION] = value }
    }

    suspend fun getApiDeviceId(): Long =
        dataStore.data.first()[PreferenceKeys.API_DEVICE_ID] ?: 0L

    suspend fun setApiDeviceId(value: Long) {
        dataStore.edit { it[PreferenceKeys.API_DEVICE_ID] = value }
    }

    suspend fun getDeviceSerial(): String =
        dataStore.data.first()[PreferenceKeys.DEVICE_SERIAL] ?: ""

    suspend fun setDeviceSerial(value: String) {
        dataStore.edit { it[PreferenceKeys.DEVICE_SERIAL] = value }
    }

    suspend fun getOrganizerSlug(): String =
        dataStore.data.first()[PreferenceKeys.ORGANIZER_SLUG] ?: ""

    suspend fun setOrganizerSlug(value: String) {
        dataStore.edit { it[PreferenceKeys.ORGANIZER_SLUG] = value }
    }

    suspend fun isConfigured(): Boolean {
        val url = getApiUrl()
        return url.isNotEmpty()
    }

    // ============================================================
    // Device Metadata
    // ============================================================

    suspend fun getDeviceKnownVersion(): Int =
        dataStore.data.first()[PreferenceKeys.DEVICE_KNOWN_VERSION] ?: 0

    suspend fun setDeviceKnownVersion(value: Int) {
        dataStore.edit { it[PreferenceKeys.DEVICE_KNOWN_VERSION] = value }
    }

    suspend fun getDeviceKnownInfo(): JSONObject {
        val jsonString = dataStore.data.first()[PreferenceKeys.DEVICE_KNOWN_INFO] ?: "{}"
        return JSONObject(jsonString)
    }

    suspend fun setDeviceKnownInfo(value: JSONObject?) {
        dataStore.edit {
            it[PreferenceKeys.DEVICE_KNOWN_INFO] = value?.toString() ?: "{}"
        }
    }

    suspend fun getDeviceKnownName(): String =
        dataStore.data.first()[PreferenceKeys.DEVICE_KNOWN_NAME] ?: ""

    suspend fun setDeviceKnownName(value: String?) {
        dataStore.edit { it[PreferenceKeys.DEVICE_KNOWN_NAME] = value ?: "" }
    }

    suspend fun getDeviceKnownGateName(): String =
        dataStore.data.first()[PreferenceKeys.DEVICE_KNOWN_GATE_NAME] ?: ""

    suspend fun setDeviceKnownGateName(value: String?) {
        dataStore.edit { it[PreferenceKeys.DEVICE_KNOWN_GATE_NAME] = value ?: "" }
    }

    suspend fun getDeviceKnownGateID(): Long =
        dataStore.data.first()[PreferenceKeys.DEVICE_KNOWN_GATE_ID] ?: 0L

    suspend fun setDeviceKnownGateID(value: Long?) {
        dataStore.edit { it[PreferenceKeys.DEVICE_KNOWN_GATE_ID] = value ?: 0L }
    }

    suspend fun getKnownPretixVersion(): Long =
        dataStore.data.first()[PreferenceKeys.PRETIX_KNOWN_VERSION] ?: 0L

    suspend fun setKnownPretixVersion(value: Long) {
        dataStore.edit { it[PreferenceKeys.PRETIX_KNOWN_VERSION] = value }
    }

    // ============================================================
    // Event Configuration (Multi-Event Support)
    // ============================================================

    suspend fun getEventSelections(): List<EventSelection> {
        val jsonString = dataStore.data.first()[PreferenceKeys.EVENT_SELECTIONS]
            ?: return emptyList()

        return try {
            json.decodeFromString(
                ListSerializer(EventSelection.serializer()),
                jsonString
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun setEventSelections(value: List<EventSelection>) {
        val jsonString = json.encodeToString(
            ListSerializer(EventSelection.serializer()),
            value
        )
        dataStore.edit { it[PreferenceKeys.EVENT_SELECTIONS] = jsonString }
    }

    suspend fun getActiveEventIndex(): Int =
        dataStore.data.first()[PreferenceKeys.ACTIVE_EVENT_INDEX] ?: 0

    suspend fun setActiveEventIndex(value: Int) {
        dataStore.edit { it[PreferenceKeys.ACTIVE_EVENT_INDEX] = value }
    }

    suspend fun getActiveEvent(): EventSelection? {
        val selections = getEventSelections()
        val index = getActiveEventIndex()
        return selections.getOrNull(index)
    }

    suspend fun getSynchronizedEvents(): List<String> {
        return getEventSelections().map { it.eventSlug }
    }

    suspend fun getSelectedSubeventForEvent(event: String): Long? {
        return getEventSelections().find { it.eventSlug == event }?.subEventId
    }

    suspend fun getSelectedCheckinListForEvent(event: String?): Long {
        if (event == null) return getActiveEvent()?.checkInListId ?: 0
        return getEventSelections().find { it.eventSlug == event }?.checkInListId ?: 0
    }

    // ============================================================
    // Sync State
    // ============================================================

    suspend fun getLastSync(): Long =
        dataStore.data.first()[PreferenceKeys.LAST_SYNC] ?: 0L

    suspend fun setLastSync(value: Long) {
        dataStore.edit { it[PreferenceKeys.LAST_SYNC] = value }
    }

    suspend fun getLastDownload(): Long =
        dataStore.data.first()[PreferenceKeys.LAST_DOWNLOAD] ?: 0L

    suspend fun setLastDownload(value: Long) {
        dataStore.edit { it[PreferenceKeys.LAST_DOWNLOAD] = value }
    }

    suspend fun getLastCleanup(): Long =
        dataStore.data.first()[PreferenceKeys.LAST_CLEANUP] ?: 0L

    suspend fun setLastCleanup(value: Long) {
        dataStore.edit { it[PreferenceKeys.LAST_CLEANUP] = value }
    }

    suspend fun getLastFailedSync(): Long =
        dataStore.data.first()[PreferenceKeys.LAST_FAILED_SYNC] ?: 0L

    suspend fun setLastFailedSync(value: Long) {
        dataStore.edit { it[PreferenceKeys.LAST_FAILED_SYNC] = value }
    }

    suspend fun getLastFailedSyncMsg(): String =
        dataStore.data.first()[PreferenceKeys.LAST_FAILED_SYNC_MSG] ?: ""

    suspend fun setLastFailedSyncMsg(value: String?) {
        dataStore.edit { it[PreferenceKeys.LAST_FAILED_SYNC_MSG] = value ?: "" }
    }

    suspend fun getKnownLiveEventSlugs(): Set<String> {
        val jsonString = dataStore.data.first()[PreferenceKeys.KNOWN_LIVE_EVENT_SLUGS] ?: "[]"
        return try {
            json.decodeFromString(
                ListSerializer(String.serializer()),
                jsonString
            ).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun setKnownLiveEventSlugs(slugs: Set<String>) {
        val jsonString = json.encodeToString(
            ListSerializer(String.serializer()),
            slugs.toList()
        )
        dataStore.edit { it[PreferenceKeys.KNOWN_LIVE_EVENT_SLUGS] = jsonString }
    }

    // ============================================================
    // Mode Flags
    // ============================================================

    suspend fun getOfflineMode(): Boolean =
        dataStore.data.first()[PreferenceKeys.OFFLINE_MODE] ?: false

    suspend fun setOfflineMode(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.OFFLINE_MODE] = value }
    }

    suspend fun getProxyMode(): Boolean =
        dataStore.data.first()[PreferenceKeys.PROXY_MODE] ?: false

    suspend fun setProxyMode(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.PROXY_MODE] = value }
    }

    // ============================================================
    // UI Preferences
    // ============================================================

    suspend fun getPlaySound(): Boolean =
        dataStore.data.first()[PreferenceKeys.PLAY_SOUND] ?: true

    suspend fun setPlaySound(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.PLAY_SOUND] = value }
    }

    suspend fun getUiHideNames(): Boolean =
        dataStore.data.first()[PreferenceKeys.UI_HIDE_NAMES] ?: true

    suspend fun setUiHideNames(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.UI_HIDE_NAMES] = value }
    }

    suspend fun getUiReduceMotion(): Boolean =
        dataStore.data.first()[PreferenceKeys.UI_REDUCE_MOTION] ?: false

    suspend fun setUiReduceMotion(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.UI_REDUCE_MOTION] = value }
    }

    suspend fun getAutoPrintBadges(): Boolean =
        dataStore.data.first()[PreferenceKeys.AUTO_PRINT_BADGES] ?: false

    suspend fun setAutoPrintBadges(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.AUTO_PRINT_BADGES] = value }
    }

    // ============================================================
    // Hardware Settings
    // ============================================================

    suspend fun getPreferredCameraName(): String =
        dataStore.data.first()[PreferenceKeys.PREFERRED_CAMERA_NAME] ?: "-"

    suspend fun setPreferredCameraName(value: String) {
        dataStore.edit { it[PreferenceKeys.PREFERRED_CAMERA_NAME] = value }
    }

    suspend fun getScanType(): String =
        dataStore.data.first()[PreferenceKeys.SCAN_TYPE] ?: "entry"

    suspend fun setScanType(value: String) {
        dataStore.edit { it[PreferenceKeys.SCAN_TYPE] = value }
    }

    suspend fun getBadgePrinterName(): String? =
        dataStore.data.first()[PreferenceKeys.BADGE_PRINTER_NAME]

    suspend fun setBadgePrinterName(value: String?) {
        dataStore.edit { prefs ->
            if (value != null) {
                prefs[PreferenceKeys.BADGE_PRINTER_NAME] = value
            } else {
                prefs.remove(PreferenceKeys.BADGE_PRINTER_NAME)
            }
        }
    }

    suspend fun getBadgePrinterOrientation(): String =
        dataStore.data.first()[PreferenceKeys.BADGE_PRINTER_ORIENTATION] ?: "Auto"

    suspend fun setBadgePrinterOrientation(value: String) {
        dataStore.edit { it[PreferenceKeys.BADGE_PRINTER_ORIENTATION] = value }
    }

    // ============================================================
    // Feature Flags
    // ============================================================

    suspend fun getPrintBadges(): Boolean =
        dataStore.data.first()[PreferenceKeys.PRINT_BADGES] ?: false

    suspend fun setPrintBadges(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.PRINT_BADGES] = value }
    }

    suspend fun getSyncOrders(): Boolean =
        dataStore.data.first()[PreferenceKeys.SYNC_ORDERS] ?: true

    suspend fun setSyncOrders(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.SYNC_ORDERS] = value }
    }

    suspend fun getSyncAuto(): Boolean =
        dataStore.data.first()[PreferenceKeys.SYNC_AUTO] ?: true

    suspend fun setSyncAuto(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.SYNC_AUTO] = value }
    }

    // ============================================================
    // Legacy Single-Event Storage
    // ============================================================
    // These properties coexist with eventSelections for backward compatibility
    // during the login flow before eventSelections is finalized

    suspend fun getEventSlug(): String? =
        dataStore.data.first()[PreferenceKeys.EVENT_SLUG]

    suspend fun setEventSlug(value: String?) {
        dataStore.edit {
            if (value != null) {
                it[PreferenceKeys.EVENT_SLUG] = value
            } else {
                it.remove(PreferenceKeys.EVENT_SLUG)
            }
        }
    }

    suspend fun getSubEventId(): Long? {
        val value = dataStore.data.first()[PreferenceKeys.SUB_EVENT_ID] ?: 0L
        return if (value == 0L) null else value
    }

    suspend fun setSubEventId(value: Long?) {
        dataStore.edit { it[PreferenceKeys.SUB_EVENT_ID] = value ?: 0L }
    }

    suspend fun getEventName(): String? =
        dataStore.data.first()[PreferenceKeys.EVENT_NAME]

    suspend fun setEventName(value: String?) {
        dataStore.edit {
            if (value != null) {
                it[PreferenceKeys.EVENT_NAME] = value
            } else {
                it.remove(PreferenceKeys.EVENT_NAME)
            }
        }
    }

    suspend fun getCheckInListId(): Long =
        dataStore.data.first()[PreferenceKeys.CHECKIN_LIST_ID] ?: 0L

    suspend fun setCheckInListId(value: Long) {
        dataStore.edit { it[PreferenceKeys.CHECKIN_LIST_ID] = value }
    }

    suspend fun getCheckInListName(): String =
        dataStore.data.first()[PreferenceKeys.CHECKIN_LIST_NAME] ?: ""

    suspend fun setCheckInListName(value: String) {
        dataStore.edit { it[PreferenceKeys.CHECKIN_LIST_NAME] = value }
    }

    // ============================================================
    // Bulk Operations
    // ============================================================

    suspend fun setDeviceConfig(
        url: String,
        key: String,
        orgaSlug: String,
        deviceId: Long,
        serial: String,
        sentVersion: Int
    ) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.API_URL] = url
            prefs[PreferenceKeys.API_KEY] = key
            prefs[PreferenceKeys.DEVICE_SERIAL] = serial
            prefs[PreferenceKeys.API_DEVICE_ID] = deviceId
            prefs[PreferenceKeys.ORGANIZER_SLUG] = orgaSlug
            prefs[PreferenceKeys.DEVICE_KNOWN_VERSION] = sentVersion

            // Clear sync state
            prefs.remove(PreferenceKeys.LAST_DOWNLOAD)
            prefs.remove(PreferenceKeys.LAST_CLEANUP)
            prefs.remove(PreferenceKeys.LAST_SYNC)
            prefs.remove(PreferenceKeys.LAST_FAILED_SYNC)
        }
    }

    suspend fun resetEventConfig() {
        dataStore.edit { it.clear() }
    }
}
