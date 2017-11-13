package eu.pretix.pretixdesk

import eu.pretix.libpretixsync.api.PretixApi
import eu.pretix.libpretixsync.config.ConfigStore
import java.util.prefs.Preferences


class PretixDeskConfig : ConfigStore {
    private val prefs = Preferences.userNodeForPackage(PretixDeskConfig::class.java)

    private val PREFS_KEY_API_URL = "pretix_api_url"
    private val PREFS_KEY_API_KEY = "pretix_api_key"
    private val PREFS_KEY_SHOW_INFO = "show_info"
    private val PREFS_KEY_ALLOW_SEARCH = "allow_search"
    private val PREFS_KEY_API_VERSION = "pretix_api_version"
    private val PREFS_KEY_ASYNC_MODE = "async"
    private val PREFS_KEY_LAST_SYNC = "last_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC = "last_failed_sync"
    private val PREFS_KEY_LAST_FAILED_SYNC_MSG = "last_failed_sync_msg"
    private val PREFS_KEY_LAST_DOWNLOAD = "last_download"
    private val PREFS_KEY_LAST_STATUS_DATA = "last_status_data"

    fun setEventConfig(url: String, key: String, version: Int, show_info: Boolean, allow_search: Boolean) {
        prefs.putByteArray(PREFS_KEY_API_URL, url.toByteArray())
        prefs.putByteArray(PREFS_KEY_API_KEY, key.toByteArray())
        prefs.putBoolean(PREFS_KEY_ALLOW_SEARCH, allow_search)
        prefs.putBoolean(PREFS_KEY_SHOW_INFO, show_info)
        prefs.putInt(PREFS_KEY_API_VERSION, version)
        prefs.remove(PREFS_KEY_LAST_DOWNLOAD)
        prefs.remove(PREFS_KEY_LAST_SYNC)
        prefs.remove(PREFS_KEY_LAST_FAILED_SYNC)
        prefs.remove(PREFS_KEY_LAST_STATUS_DATA)
        prefs.flush();
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
        prefs.flush()
    }

    fun getAsyncModeEnabled(): Boolean {
        return prefs.getBoolean(PREFS_KEY_ASYNC_MODE, false)
    }

    fun setAsyncModeEnabled(value: Boolean) {
        prefs.putBoolean(PREFS_KEY_ASYNC_MODE, value)
        prefs.flush()
    }

    override fun isDebug(): Boolean {
        return false;
    }

    override fun isConfigured(): Boolean {
        return prefs.getByteArray(PREFS_KEY_API_URL, null) != null
    }

    override fun getApiVersion(): Int {
        return prefs.getInt(PREFS_KEY_API_VERSION, PretixApi.SUPPORTED_API_VERSION)
    }

    override fun getApiUrl(): String {
        return String(prefs.getByteArray(PREFS_KEY_API_URL, kotlin.ByteArray(0)))
    }

    override fun getApiKey(): String {
        return String(prefs.getByteArray(PREFS_KEY_API_KEY, kotlin.ByteArray(0)))
    }

    override fun getShowInfo(): Boolean {
        return prefs.getBoolean(PREFS_KEY_SHOW_INFO, true)
    }

    override fun getAllowSearch(): Boolean {
        return prefs.getBoolean(PREFS_KEY_ALLOW_SEARCH, true)
    }

    override fun getLastStatusData(): String {
        return String(prefs.getByteArray(PREFS_KEY_LAST_STATUS_DATA, kotlin.ByteArray(0)))
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
        return String(prefs.getByteArray(PREFS_KEY_LAST_FAILED_SYNC_MSG, kotlin.ByteArray(0)))
    }

    override fun setLastFailedSyncMsg(value: String?) {
        prefs.putByteArray(PREFS_KEY_LAST_FAILED_SYNC_MSG, value?.toByteArray())
        prefs.flush()
    }
}