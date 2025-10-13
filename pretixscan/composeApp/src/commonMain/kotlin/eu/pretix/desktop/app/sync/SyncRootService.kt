package eu.pretix.desktop.app.sync

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.sync.SyncManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.context.GlobalContext
import java.util.logging.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Coordinates sync across the app.
 */
class SyncRootService(private val appConfig: DataStoreConfigStore) : ViewModel() {
    private val log = Logger.getLogger("SyncRootService")

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _minimumSyncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val minimumSyncState: StateFlow<SyncState> = _minimumSyncState.asStateFlow()

    private val _eventSyncStates = MutableStateFlow<Map<String, EventSyncState>>(emptyMap())
    val eventSyncStates: StateFlow<Map<String, EventSyncState>> = _eventSyncStates.asStateFlow()

    private val _showMainSyncProgress = MutableStateFlow(false)
    val showMainSyncProgress: StateFlow<Boolean> = _showMainSyncProgress.asStateFlow()

    private var shouldSync: Boolean = false
    private var mainSyncJob: Job? = null

    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    init {
        log.info("Starting main sync job loop")
        tickerFlow(5.seconds)
            .filter { shouldSync }
            .onEach {
                mainSyncJob?.cancel()
                mainSyncJob = viewModelScope.launch(Dispatchers.IO) {
                    executeSync()
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope) // the returned job is not used, the viewModelScope is responsible for cancellation
    }

    private fun executeSync(force: Boolean = false, nowMillis: Long = System.currentTimeMillis()) {
        try {
            if (!appConfig.isConfigured) {
                log.info("skip sync while logged out")
                _syncState.value = SyncState.Idle
                return
            }
            if (!appConfig.syncAuto && !force) {
                log.info("skip sync, auto-sync disabled")
                _syncState.value = SyncState.Idle
                return
            }

            val events = appConfig.eventSelections
            if (events.isEmpty()) {
                log.info("no events to sync")
                _syncState.value = SyncState.Idle
                return
            }

            log.info("performing sync for ${events.size} events")

            // Initialize event sync states
            _eventSyncStates.value = events.associate {
                it.eventSlug to EventSyncState.Pending
            }

            _syncState.value = SyncState.InProgress("Syncing ${events.size} event(s)...")
            val syncManager = GlobalContext.get().get<SyncManager>()

            val syncResult = syncManager.sync(force) { message ->
                runBlocking {
                    withContext(Dispatchers.Main) {
                        _syncState.value = SyncState.InProgress(message)

                        // Update per-event state based on message content
                        events.forEach { event ->
                            if (message.contains(event.eventSlug, ignoreCase = true) ||
                                message.contains(event.eventName, ignoreCase = true)) {
                                _eventSyncStates.update { states ->
                                    states + (event.eventSlug to EventSyncState.InProgress(message))
                                }
                            }
                        }
                    }
                }
            }

            if (syncResult.exception != null) {
                log.warning("sync failed, rethrowing: $syncResult")
                throw syncResult.exception
            }

            // Mark all as success
            _eventSyncStates.value = events.associate {
                it.eventSlug to EventSyncState.Success
            }

            _syncState.value = SyncState.Success(lastSync = nowMillis)
            appConfig.lastFailedSync = 0L
            appConfig.lastSync = nowMillis
            appConfig.lastDownload = nowMillis
        } catch (e: Exception) {
            log.warning("sync failed: ${e.stackTraceToString()}")

            // Mark in-progress events as error
            _eventSyncStates.update { states ->
                states.mapValues { (_, state) ->
                    when (state) {
                        is EventSyncState.InProgress -> EventSyncState.Error(e.localizedMessage ?: "Sync failed")
                        EventSyncState.Pending -> EventSyncState.Error(e.localizedMessage ?: "Sync failed")
                        else -> state
                    }
                }
            }

            _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown error")
            appConfig.lastFailedSync = nowMillis
            appConfig.lastFailedSyncMsg = e.localizedMessage ?: "Unknown error"
        }
    }

    fun onRouteChanged(route: String) {
        log.info("Route changed ${route}")
        _showMainSyncProgress.value = route == "/eu/pretix/scan/main"
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

    suspend fun minimalSync(nowMillis: Long = System.currentTimeMillis()) {
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
                _minimumSyncState.value = SyncState.Success(lastSync = nowMillis)
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

    fun forceSync(nowMillis: Long = System.currentTimeMillis()) {
        log.info("Full sync requested")
        val shouldResume = skipFutureSyncs()
        runBlocking {
            mainSyncJob?.cancel()
            mainSyncJob = viewModelScope.launch(Dispatchers.IO) {
                executeSync(true, nowMillis)
            }
        }
        log.info("Full sync completed")
        if (shouldResume) {
            resumeSync()
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    data class InProgress(val message: String) : SyncState()
    data class Success(val lastSync: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

sealed class EventSyncState {
    object Pending : EventSyncState()
    data class InProgress(val message: String) : EventSyncState()
    object Success : EventSyncState()
    data class Error(val message: String) : EventSyncState()
}

val LocalSyncRootService = compositionLocalOf<SyncRootService> {
    error("No SyncRootService found! Make sure to provide it in SyncRoot.")
}