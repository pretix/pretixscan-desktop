package eu.pretix.scan.tickets.data

import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.pretix.libpretixsync.SentryInterface
import eu.pretix.libpretixsync.check.QuestionType
import eu.pretix.libpretixsync.check.TicketCheckProvider
import eu.pretix.libpretixsync.db.Answer
import eu.pretix.libpretixsync.models.Question
import eu.pretix.libpretixsync.sqldelight.SyncDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TicketCodeHandlerTest {

    private lateinit var conf: DataStoreConfigStore
    private lateinit var appCache: AppCache
    private lateinit var db: SyncDatabase
    private lateinit var checkProvider: TicketCheckProvider
    private lateinit var audioPlayer: GadulkaPlayer
    private lateinit var sentryInterface: SentryInterface
    private lateinit var connectivityHelper: ConnectivityHelper
    private lateinit var layoutFetcher: PrintLayoutFetcher

    private var capturedAnswers: List<Answer>? = null
    private var capturedAllowQuestions: Boolean = false
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        conf = mockk(relaxed = true)
        appCache = mockk(relaxed = true)
        db = mockk(relaxed = true)
        checkProvider = mockk(relaxed = true)
        audioPlayer = mockk(relaxed = true)
        sentryInterface = mockk(relaxed = true)
        connectivityHelper = mockk(relaxed = true)
        layoutFetcher = mockk(relaxed = true)

        every { conf.playSound } returns false
        every { conf.scanType } returns "entry"
        every { conf.printBadges } returns false
        every { conf.eventSelectionToMap() } returns mapOf("slug" to 1L)
        every { conf.unpaidAsk } returns true

        every { appCache.db } returns db

        capturedAnswers = null
        capturedAllowQuestions = false

        every {
            checkProvider.check(
                any(),
                ticketid = any(),
                source_type = any(),
                answers = any(),
                ignore_unpaid = any(),
                with_badge_data = any(),
                any(),
                nonce = any(),
                allowQuestions = any(),
            )
        } answers {
            @Suppress("UNCHECKED_CAST")
            capturedAnswers = args[3] as? List<Answer>
            capturedAllowQuestions = args[8] as Boolean
            TicketCheckProvider.CheckResult(TicketCheckProvider.CheckResult.Type.VALID)
        }
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun makeQuestion(serverId: Long): Question = Question(
        id = serverId,
        serverId = serverId,
        eventSlug = "slug",
        position = 0L,
        required = true,
        question = "Question $serverId",
        identifier = "q$serverId",
        askDuringCheckIn = true,
        showDuringCheckIn = true,
        type = QuestionType.S,
    ).also { it.resolveDependency(emptyList()) }

    private fun createHandler() = TicketCodeHandler(
        conf = conf,
        appCache = appCache,
        checkProviderFactory = { checkProvider },
        audioPlayer = audioPlayer,
        logHandler = sentryInterface,
        connectivityHelper = connectivityHelper,
        layoutFetcher = layoutFetcher,
    )

    @Test
    fun `allowQuestions is always true regardless of whether answers are provided`() = runTest {
        createHandler().handleScan("S", answers = null, ignoreUnpaid = true)
        testScheduler.advanceUntilIdle()
        assertEquals(true, capturedAllowQuestions)
    }

    @Test
    fun `allowQuestions is true when user supplies answers`() = runTest {
        val answer = Answer(makeQuestion(123L), "foo")

        createHandler().handleScan("S", answers = listOf(answer), ignoreUnpaid = true)
        testScheduler.advanceUntilIdle()

        assertEquals(true, capturedAllowQuestions)
    }

    @Test
    fun `answers are forwarded directly to check provider without modification`() = runTest {
        val q = makeQuestion(42L)
        val answer = Answer(q, "some-value")

        createHandler().handleScan("S", answers = listOf(answer), ignoreUnpaid = true)
        testScheduler.advanceUntilIdle()

        val captured = capturedAnswers
        assertNotNull(captured)
        assertEquals(1, captured.size)
        assertSame(answer, captured[0])
    }

    @Test
    fun `multiple answers are forwarded directly without rebuilding`() = runTest {
        val q1 = makeQuestion(10L)
        val q2 = makeQuestion(20L)
        val answer1 = Answer(q1, "value-10")
        val answer2 = Answer(q2, "value-20")

        createHandler().handleScan("S", answers = listOf(answer1, answer2), ignoreUnpaid = true)
        testScheduler.advanceUntilIdle()

        val captured = capturedAnswers
        assertNotNull(captured)
        assertEquals(2, captured.size)
        assertSame(answer1, captured[0])
        assertSame(answer2, captured[1])
    }

    @Test
    fun `null answers are passed through as null`() = runTest {
        createHandler().handleScan("S", answers = null, ignoreUnpaid = false)
        testScheduler.advanceUntilIdle()

        val captured = capturedAnswers
        assertTrue(captured == null)
    }

    @Test
    fun `answer question objects retain their original identity through to the provider`() = runTest {
        val q = makeQuestion(99L)
        val answer = Answer(q, "answer-value")

        createHandler().handleScan("S", answers = listOf(answer), ignoreUnpaid = true)
        testScheduler.advanceUntilIdle()

        val captured = capturedAnswers
        assertNotNull(captured)
        assertSame(q, captured[0].question)
    }
}
