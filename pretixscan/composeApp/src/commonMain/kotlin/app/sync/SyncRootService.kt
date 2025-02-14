package app.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.AppConfig
import eu.pretix.libpretixsync.sync.SyncManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.logging.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Coordinates sync accross the app.
 *
 *
 */
class SyncRootService(
    private val appConfig: AppConfig,
    private val appCache: AppCache,
    private val syncManager: SyncManager
) : ViewModel() {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    private val log = Logger.getLogger("SyncRootService")
    private var shouldSync: Boolean = false

    private var job: Job? = null

    fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    init {
        log.info("Starting a new sync loop")
        job = tickerFlow(5.seconds)
            .filter { shouldSync }
            .map {
                log.info("sync tick")
                _syncState.value = SyncState.InProgress("")
                try {
                    syncManager.sync(false) {
                        _syncState.value = SyncState.InProgress(it ?: "")
                    }
                    _syncState.value = SyncState.Success(lastSync = LocalDateTime.now().toString())
                } catch (e: Exception) {
                    log.warning("sync failed: ${e.stackTraceToString()}")
                    _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
            .launchIn(viewModelScope)
    }

//    override fun onCleared() {
//        log.info("clea")
//        super.onCleared()
//        job?.cancel()
//    }

    fun onRouteChanged(route: String) {
        log.info("Route changed ${route}")
    }

    fun pauseSync(): Boolean {
        log.info("Pausing sync")
        val shouldResume = shouldSync
        shouldSync = false
        return shouldResume
    }

    fun resumeSync() {
        log.info("Resumming sync")
        shouldSync = true
    }

//    private suspend fun performSync() = withContext(Dispatchers.IO) {
//        log.info("Sync loop begins now")
//
//        while(this.isActive) {
//            while (shouldSync) {
//                _syncState.value = SyncState.InProgress("")
//                try {
//                    log.info("... sync")
//                    syncManager.sync(false) {
//                        _syncState.value = SyncState.InProgress(it ?: "")
//                    }
//                    _syncState.value = SyncState.Success(lastSync = LocalDateTime.now().toString())
//                } catch (e: Exception) {
//                    log.warning("sync failed: ${e.stackTraceToString()}")
//                    _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
//                }
//
//                delay(2_000)
//            }
//        }
//        log.info("Sync loop ended")
//    }

    fun minimalSync() {
        log.info("Running a minimal sync")
        val shouldResume = pauseSync()
        _syncState.value = SyncState.InProgress("")

        runBlocking {
            try {
                syncManager.syncMinimalEventSet(appConfig.eventSlug, appConfig.subEventId ?: 0L) {
                    _syncState.value = SyncState.InProgress(it ?: "")
                }
                _syncState.value = SyncState.Success(lastSync = LocalDateTime.now().toString())
                log.info("Minimal sync completed")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
            }
        }

        if (shouldResume) {
            resumeSync()
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    data class InProgress(val message: String) : SyncState()
    data class Success(val lastSync: String) : SyncState()
    data class Error(val message: String) : SyncState()
}