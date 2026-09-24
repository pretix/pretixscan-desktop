package eu.pretix.desktop.nfc

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.desktop.cache.LocalCacheFactory
import eu.pretix.desktop.cache.createSyncDatabase
import eu.pretix.libpretixnfc.communication.ChipReadError
import eu.pretix.libpretixnfc.platform.HardwareBackedKeyStore
import eu.pretix.libpretixsync.db.ReusableMediaType
import eu.pretix.libpretixsync.models.Settings
import eu.pretix.libpretixsync.sqldelight.AndroidUtilDateAdapter
import eu.pretix.libpretixsync.sqldelight.BigDecimalAdapter
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import eu.pretix.libpretixsync.utils.SettingsManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import java.nio.ByteBuffer
import javax.smartcardio.ATR
import javax.smartcardio.Card
import javax.smartcardio.CardChannel
import javax.smartcardio.CardException
import javax.smartcardio.CardTerminal
import javax.smartcardio.CommandAPDU
import javax.smartcardio.ResponseAPDU
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PcscNfcReaderServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    private class InMemoryCacheFactory : LocalCacheFactory {
        override fun deleteDataSource() {}

        override fun getSyncDataSource(): SyncDatabase = createSyncDatabase(
            driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, schema = SyncDatabase.Schema),
            dateAdapter = AndroidUtilDateAdapter(),
            bigDecimalAdapter = BigDecimalAdapter(),
        )
    }

    private class FixedSettingsManager(private val json: JSONObject) : SettingsManager {
        override fun getBySlug(eventSlug: String): Settings =
            Settings(id = 1L, slug = eventSlug, json = json)
    }

    private object UnusedKeyStore : HardwareBackedKeyStore {
        override fun hasHmacKey(keyName: String): Boolean = throw UnsupportedOperationException()
        override fun importHmacKey(keyName: String, keyValue: ByteArray) = throw UnsupportedOperationException()
        override fun hmacSHA256(keyName: String, message: ByteArray): ByteArray = throw UnsupportedOperationException()
        override fun getOrCreateRsaPubKey(keyName: String): ByteArray = throw UnsupportedOperationException()
        override fun decryptRsa(keyName: String, ciphertext: ByteArray): ByteArray = throw UnsupportedOperationException()
    }

    private class FakeCardChannel(private val card: Card, private val uid: ByteArray) : CardChannel() {
        override fun getCard(): Card = card
        override fun getChannelNumber(): Int = 0
        override fun transmit(command: CommandAPDU): ResponseAPDU =
            ResponseAPDU(uid + byteArrayOf(0x90.toByte(), 0x00))

        override fun transmit(command: ByteBuffer, response: ByteBuffer): Int =
            throw UnsupportedOperationException()

        override fun close() {}
    }

    private class FakeCard(uid: ByteArray) : Card() {
        var disconnected = false
        private val channel = FakeCardChannel(this, uid)

        override fun getATR(): ATR = ATR(byteArrayOf(0x3B))
        override fun getProtocol(): String = "T=1"
        override fun getBasicChannel(): CardChannel = channel
        override fun openLogicalChannel(): CardChannel = throw UnsupportedOperationException()
        override fun beginExclusive() {}
        override fun endExclusive() {}
        override fun transmitControlCommand(controlCode: Int, command: ByteArray): ByteArray =
            throw UnsupportedOperationException()

        override fun disconnect(reset: Boolean) {
            disconnected = true
        }
    }

    private class FakeCardTerminal(
        private val terminalName: String,
        private val uid: ByteArray,
        var cardPresent: Boolean = true,
    ) : CardTerminal() {
        val cards = mutableListOf<FakeCard>()

        override fun getName(): String = terminalName

        override fun connect(protocol: String): Card = FakeCard(uid).also { cards.add(it) }

        override fun isCardPresent(): Boolean = cardPresent

        override fun waitForCardPresent(timeout: Long): Boolean = throw UnsupportedOperationException()

        override fun waitForCardAbsent(timeout: Long): Boolean = throw UnsupportedOperationException()
    }

    private fun createConfig(): DataStoreConfigStore {
        val config = mockk<DataStoreConfigStore>(relaxed = true)
        every { config.synchronizedEvents } returns listOf("demo")
        every { config.nfcReaderName } returns null
        every { config.organizerSlug } returns "demo-orga"
        return config
    }

    private fun createService(
        dispatcher: CoroutineDispatcher,
        settings: JSONObject = JSONObject(mapOf("reusable_media_type_nfc_uid" to true)),
        config: DataStoreConfigStore = createConfig(),
        terminals: () -> List<CardTerminal>,
    ) = PcscNfcReaderService(
        appConfig = config,
        appCache = AppCache(InMemoryCacheFactory()),
        settingsManager = FixedSettingsManager(settings),
        keyStore = UnusedKeyStore,
        dispatcher = dispatcher,
        terminals = terminals,
    )

    @Test
    fun `a chip held against the reader is reported with its uid`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(testDispatcher) { listOf(terminal) }
        val events = mutableListOf<NfcReadEvent>()
        backgroundScope.launch { service.events.collect { events.add(it) } }
        runCurrent()

        service.start()
        advanceTimeBy(1000)

        assertEquals(NfcState.RUNNING, service.state.value)
        assertEquals(listOf<NfcReadEvent>(NfcReadEvent.Success("04AABBCC", ReusableMediaType.NFC_UID)), events)
        assertTrue(terminal.cards.all { it.disconnected })

        service.stop()
        advanceTimeBy(1000)
        assertEquals(NfcState.STOPPED, service.state.value)
    }

    @Test
    fun `the same chip is only reported once while the debounce lasts`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(testDispatcher) { listOf(terminal) }
        val events = mutableListOf<NfcReadEvent>()
        backgroundScope.launch { service.events.collect { events.add(it) } }
        runCurrent()

        service.start()
        advanceTimeBy(1000)
        terminal.cardPresent = false
        advanceTimeBy(1000)
        terminal.cardPresent = true
        advanceTimeBy(1000)

        assertEquals(1, events.size)

        service.stop()
        advanceTimeBy(1000)
    }

    @Test
    fun `without an attached reader the service reports itself as disabled`() = runTest(testDispatcher) {
        val service = createService(testDispatcher) { emptyList() }

        service.start()
        advanceTimeBy(1000)

        assertEquals(NfcState.DISABLED, service.state.value)

        service.stop()
        advanceTimeBy(1000)
    }

    @Test
    fun `without a smart card service the reader reports itself as unsupported`() = runTest(testDispatcher) {
        val service = createService(testDispatcher) { throw CardException("no PC_SC service") }

        service.start()
        advanceTimeBy(1000)

        assertEquals(NfcState.UNSUPPORTED, service.state.value)

        service.stop()
        advanceTimeBy(1000)
    }

    @Test
    fun `stopping ends the poll loop so no further chip is read`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(testDispatcher) { listOf(terminal) }

        service.start()
        advanceTimeBy(1000)
        service.stop()
        advanceTimeBy(1000)

        val readsBeforeStop = terminal.cards.size
        advanceTimeBy(5000)

        assertEquals(readsBeforeStop, terminal.cards.size)
        assertEquals(NfcState.STOPPED, service.state.value)
    }

    @Test
    fun `a plain chip is rejected when the event asks for Mifare Ultralight AES`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(
            testDispatcher,
            settings = JSONObject(mapOf("reusable_media_type_nfc_mf0aes" to true)),
        ) { listOf(terminal) }
        val events = mutableListOf<NfcReadEvent>()
        backgroundScope.launch { service.events.collect { events.add(it) } }
        runCurrent()

        service.start()
        advanceTimeBy(1000)

        assertEquals(
            listOf<NfcReadEvent>(NfcReadEvent.Error(ChipReadError.IO_ERROR)),
            events
        )

        service.stop()
        advanceTimeBy(1000)
    }

    @Test
    fun `a chip that could not be read is reported again when it is presented once more`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(
            testDispatcher,
            settings = JSONObject(mapOf("reusable_media_type_nfc_mf0aes" to true)),
        ) { listOf(terminal) }
        val events = mutableListOf<NfcReadEvent>()
        backgroundScope.launch { service.events.collect { events.add(it) } }
        runCurrent()

        service.start()
        advanceTimeBy(1000)
        terminal.cardPresent = false
        advanceTimeBy(1000)
        terminal.cardPresent = true
        advanceTimeBy(1000)
        service.stop()
        advanceTimeBy(1000)

        assertEquals(
            listOf<NfcReadEvent>(
                NfcReadEvent.Error(ChipReadError.IO_ERROR),
                NfcReadEvent.Error(ChipReadError.IO_ERROR),
            ),
            events
        )
    }

    @Test
    fun `an event without an NFC media type keeps the reader stopped`() = runTest(testDispatcher) {
        val terminal = FakeCardTerminal("ACS ACR122U", uid = byteArrayOf(0x04, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte()))
        val service = createService(
            testDispatcher,
            settings = JSONObject(mapOf("reusable_media_type_barcode" to true)),
        ) { listOf(terminal) }

        service.start()
        advanceTimeBy(1000)

        assertEquals(NfcState.STOPPED, service.state.value)
        assertEquals(0, terminal.cards.size)
    }
}
