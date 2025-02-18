package app.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.*
import org.koin.core.context.GlobalContext

/**
 * Coordinates sync across the app.
 *
 *
 */
class SyncRootService(
    private val appConfig: AppConfig
) : ViewModel() {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _minimumSyncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val minimumSyncState: StateFlow<SyncState> = _syncState.asStateFlow()


    private val _showMainSyncProgress = MutableStateFlow(false)
    val showMainSyncProgress: StateFlow<Boolean> = _showMainSyncProgress.asStateFlow()

    private val log = Logger.getLogger("SyncRootService")
    private var shouldSync: Boolean = false

    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    init {
        log.info("Starting a new sync job")
        tickerFlow(5.seconds)
            .filter { shouldSync }
            .onEach {
                log.info("performing a sync")
                _syncState.value = SyncState.InProgress("")
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        if (!appConfig.isConfigured) {
                            log.info("skip sync while logged out")
                            _syncState.value = SyncState.Idle
                            return@launch
                        }
                        val syncManager = GlobalContext.get().get<SyncManager>()
                        syncManager.sync(true) {
                            println("sync progress $it")
                            runBlocking {
                                withContext(Dispatchers.Main) {
                                    _syncState.value = SyncState.InProgress(it)
                                }
                            }
                        }
                        _syncState.value = SyncState.Success(lastSync = LocalDateTime.now().toString())
                    } catch (e: Exception) {
                        log.warning("sync failed: ${e.stackTraceToString()}")
                        _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
                    }
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope) // the returned job is not used, the viewModelScope is responsible for cancellation
    }

    fun onRouteChanged(route: String) {
        log.info("Route changed ${route}")
        _showMainSyncProgress.value = route == "/main"
    }

    fun skipFutureSyncs(): Boolean {
        log.info("Pausing sync")
        val shouldResume = shouldSync
        shouldSync = false
        return shouldResume
    }

    fun resumeSync() {
        log.info("Resuming sync")
        shouldSync = true
    }

    suspend fun minimalSync() {
        log.info("Running a minimal sync")
        val shouldResume = skipFutureSyncs()
        _minimumSyncState.value = SyncState.InProgress("")

        withContext(Dispatchers.IO) {
            try {
                if (!appConfig.isConfigured) {
                    log.info("skip minimal sync while logged out")
                    _minimumSyncState.value = SyncState.Idle
                    return@withContext
                }
                val syncManager = GlobalContext.get().get<SyncManager>()
                syncManager.syncMinimalEventSet(appConfig.eventSlug, appConfig.subEventId ?: 0L) {
                    runBlocking {
                        withContext(Dispatchers.Main) {
                            _minimumSyncState.value = SyncState.InProgress(it)
                        }
                    }
                }
                _minimumSyncState.value = SyncState.Success(lastSync = LocalDateTime.now().toString())
                log.info("skip sync completed")
            } catch (e: Exception) {
                log.warning("minimal sync failed: ${e.stackTraceToString()}")
                _minimumSyncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
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