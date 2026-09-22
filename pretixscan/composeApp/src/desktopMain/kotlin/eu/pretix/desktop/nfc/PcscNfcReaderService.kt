package eu.pretix.desktop.nfc

import Mf0aesKeySet
import PretixMf0aes
import eu.pretix.desktop.app.ui.SelectableValue
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixnfc.communication.ChipReadError
import eu.pretix.libpretixnfc.communication.NfcChipReadError
import eu.pretix.libpretixnfc.communication.NfcIOError
import eu.pretix.libpretixnfc.desktop.hardware.PcscNfcA
import eu.pretix.libpretixnfc.platform.HardwareBackedKeyStore
import eu.pretix.libpretixsync.db.ReusableMediaType
import eu.pretix.libpretixsync.models.db.toModel
import eu.pretix.libpretixsync.utils.SettingsManager
import eu.pretix.libpretixsync.utils.codec.binary.Base64
import eu.pretix.libpretixsync.utils.getActiveMediaTypes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.logging.Logger
import javax.smartcardio.Card
import javax.smartcardio.CardException
import javax.smartcardio.CardTerminal
import javax.smartcardio.TerminalFactory

/**
 * Reads pretix media from chips held against a PC/SC reader.
 */
class PcscNfcReaderService(
    private val appConfig: DataStoreConfigStore,
    private val appCache: AppCache,
    private val settingsManager: SettingsManager,
    private val keyStore: HardwareBackedKeyStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val terminals: () -> List<CardTerminal> = { TerminalFactory.getDefault().terminals().list() },
) : NfcReaderService {
    private val log = Logger.getLogger("PcscNfcReaderService")

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val lifecycle = Mutex()

    private val _state = MutableStateFlow(NfcState.STOPPED)
    override val state: StateFlow<NfcState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NfcReadEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<NfcReadEvent> = _events.asSharedFlow()

    @Volatile
    private var wanted = false
    private var pollJob: Job? = null
    private var activeSetup: ReaderSetup? = null
    private var lastIdentifier: String? = null
    private var lastIdentifierReadAt = 0L

    override fun start() {
        wanted = true
        scope.launch {
            lifecycle.withLock {
                try {
                    val setup = if (wanted) readSetup() else null
                    if (setup == null) {
                        stopPolling()
                        return@withLock
                    }
                    if (pollJob?.isActive == true && activeSetup?.fingerprint == setup.fingerprint) {
                        return@withLock
                    }
                    pollJob?.cancelAndJoin()
                    activeSetup = setup
                    pollJob = scope.launch { poll(setup) }
                    log.info("Polling for ${setup.mediaTypes} on ${setup.readerName ?: "any reader"}")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.warning("Could not start the NFC reader: ${e.message}")
                    stopPolling()
                }
            }
        }
    }

    override fun stop() {
        wanted = false
        scope.launch {
            lifecycle.withLock {
                if (!wanted) {
                    stopPolling()
                }
            }
        }
    }

    override fun listReaders(): List<SelectableValue> =
        attachedTerminals().orEmpty().map { SelectableValue(value = it.name, label = it.name) }

    private suspend fun stopPolling() {
        pollJob?.cancelAndJoin()
        pollJob = null
        activeSetup = null
        _state.value = NfcState.STOPPED
    }

    private fun readSetup(): ReaderSetup? {
        val eventSlug = appConfig.synchronizedEvents.firstOrNull()
        val mediaTypes = getActiveMediaTypes(settingsManager, eventSlug)
        if (mediaTypes.none { it.isNfcBased() }) {
            return null
        }

        val settings = eventSlug?.let { settingsManager.getBySlug(it) }
        val useRandomIdForNewTags =
            settings?.json?.optBoolean("reusable_media_type_nfc_mf0aes_random_uid", false) ?: false
        val keySets = if (mediaTypes.contains(ReusableMediaType.NFC_MF0AES)) readKeySets() else emptyList()

        return ReaderSetup(
            mediaTypes = mediaTypes,
            keySets = keySets,
            useRandomIdForNewTags = useRandomIdForNewTags,
            readerName = appConfig.nfcReaderName,
        )
    }

    private fun readKeySets(): List<Mf0aesKeySet> =
        appCache.db.mediumKeySetQueries.selectAll().executeAsList().map { it.toModel() }.map {
            Mf0aesKeySet(
                it.publicId,
                it.organizer == appConfig.organizerSlug && it.active,
                decryptKey(it.uidKey),
                decryptKey(it.diversificationKey),
            )
        }

    private fun decryptKey(encoded: String): ByteArray =
        keyStore.decryptRsa(
            NfcReaderService.DEVICE_KEY_NAME,
            Base64.decodeBase64(encoded.toByteArray(Charsets.UTF_8)),
        )

    private suspend fun poll(setup: ReaderSetup) = coroutineScope {
        while (isActive) {
            val candidates = pollableTerminals(setup.readerName)
            if (candidates.isEmpty()) {
                delay(POLL_INTERVAL_MS)
                continue
            }
            _state.value = NfcState.RUNNING

            val terminal = candidates.firstOrNull { hasCard(it) }
            if (terminal != null) {
                readChip(terminal, setup)
                while (isActive && hasCard(terminal)) {
                    delay(POLL_INTERVAL_MS)
                }
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun pollableTerminals(readerName: String?): List<CardTerminal> {
        val attached = attachedTerminals()
        if (attached == null) {
            _state.value = NfcState.UNSUPPORTED
            return emptyList()
        }

        val pollable = if (readerName == null) attached else attached.filter { it.name == readerName }
        if (pollable.isEmpty()) {
            _state.value = NfcState.DISABLED
        }
        return pollable
    }

    private fun attachedTerminals(): List<CardTerminal>? = try {
        terminals()
    } catch (e: Exception) {
        log.warning("No smart card service available: ${e.message}")
        null
    }

    private fun hasCard(terminal: CardTerminal): Boolean = try {
        terminal.isCardPresent
    } catch (_: CardException) {
        false
    }

    private suspend fun readChip(terminal: CardTerminal, setup: ReaderSetup) {
        try {
            val card = terminal.connect(CONNECT_ANY_PROTOCOL)
            try {
                val nfca = PcscNfcA(card.basicChannel)
                val identifier = nfca.readUid()
                if (!isNewChip(identifier)) {
                    return
                }
                if (setup.mediaTypes.contains(ReusableMediaType.NFC_UID)) {
                    rememberChip(identifier)
                    _events.emit(NfcReadEvent.Success(identifier, ReusableMediaType.NFC_UID))
                    return
                }
                val encodedId = PretixMf0aes(setup.keySets, setup.useRandomIdForNewTags, false).process(nfca)
                rememberChip(identifier)
                _events.emit(NfcReadEvent.Success(encodedId, ReusableMediaType.NFC_MF0AES))
            } finally {
                disconnect(card)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: NfcChipReadError) {
            _events.emit(NfcReadEvent.Error(e.errorType))
        } catch (e: NfcIOError) {
            log.warning("Chip read failed: ${e.message}")
            _events.emit(NfcReadEvent.Error(ChipReadError.IO_ERROR))
        } catch (e: CardException) {
            log.warning("Reader communication failed: ${e.message}")
            _events.emit(NfcReadEvent.Error(ChipReadError.IO_ERROR))
        } catch (e: Exception) {
            log.warning("Chip read failed unexpectedly: ${e.message}")
            _events.emit(NfcReadEvent.Error(ChipReadError.UNKNOWN_ERROR))
        }
    }

    private fun disconnect(card: Card) {
        try {
            card.disconnect(false)
        } catch (e: CardException) {
            log.warning("Could not release the card: ${e.message}")
        }
    }

    private fun isNewChip(identifier: String): Boolean =
        identifier != lastIdentifier || System.currentTimeMillis() - lastIdentifierReadAt >= DEBOUNCE_MS

    private fun rememberChip(identifier: String) {
        lastIdentifier = identifier
        lastIdentifierReadAt = System.currentTimeMillis()
    }

    private class ReaderSetup(
        val mediaTypes: List<ReusableMediaType>,
        val keySets: List<Mf0aesKeySet>,
        val useRandomIdForNewTags: Boolean,
        val readerName: String?,
    ) {
        val fingerprint: List<Any?> = listOf(
            mediaTypes,
            keySets.map { it.publicId to it.canEncode },
            useRandomIdForNewTags,
            readerName,
        )
    }

    private companion object {
        const val POLL_INTERVAL_MS = 300L
        const val DEBOUNCE_MS = 2000L
        const val CONNECT_ANY_PROTOCOL = "*"
    }
}
