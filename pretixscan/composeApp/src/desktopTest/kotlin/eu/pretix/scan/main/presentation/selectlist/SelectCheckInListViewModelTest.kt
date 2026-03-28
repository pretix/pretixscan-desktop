package eu.pretix.scan.main.presentation.selectlist

import app.cash.sqldelight.Query
import eu.pretix.desktop.cache.AppCache
import eu.pretix.desktop.cache.DataStoreConfigStore
import eu.pretix.libpretixsync.sqldelight.CheckInList
import eu.pretix.libpretixsync.sqldelight.CheckInListQueries
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SelectCheckInListViewModelTest {

    private lateinit var appConfig: DataStoreConfigStore
    private lateinit var appCache: AppCache
    private lateinit var db: SyncDatabase
    private lateinit var checkInListQueries: CheckInListQueries
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appConfig = mockk(relaxed = true)
        appCache = mockk(relaxed = true)
        db = mockk(relaxed = true)
        checkInListQueries = mockk(relaxed = true)
        every { appCache.db } returns db
        every { db.checkInListQueries } returns checkInListQueries
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun makeList(id: Long, subEventId: Long?) = CheckInList(
        id = id,
        all_items = true,
        event_slug = "test-event",
        include_pending = false,
        json_data = null,
        name = "List $id",
        server_id = id,
        subevent_id = subEventId
    )

    private fun stubLists(lists: List<CheckInList>) {
        val query = mockk<Query<CheckInList>>()
        every { query.executeAsList() } returns lists
        every { checkInListQueries.selectByEventSlug(any()) } returns query
    }

    private fun createViewModel(subEventId: Long? = null) = SelectCheckInListViewModel(
        appCache = appCache,
        appConfig = appConfig,
        eventSlugOverride = "test-event",
        subEventIdOverride = subEventId
    )

    @Test
    fun `global list with subevent_id 0L is included when filtering by subevent`() = runTest {
        val globalList = makeList(id = 1L, subEventId = 0L)
        val subEventList = makeList(id = 2L, subEventId = 42L)
        stubLists(listOf(globalList, subEventList))

        val viewModel = createViewModel(subEventId = 42L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Selecting)
        val lists = (state as SelectCheckInListUiState.Selecting).lists
        assertTrue(lists.any { it.id == 1L }, "Global list (subevent_id=0) should be included")
        assertTrue(lists.any { it.id == 2L }, "Matching subevent list should be included")
    }

    @Test
    fun `global list with subevent_id null is included when filtering by subevent`() = runTest {
        val globalList = makeList(id = 1L, subEventId = null)
        val subEventList = makeList(id = 2L, subEventId = 42L)
        stubLists(listOf(globalList, subEventList))

        val viewModel = createViewModel(subEventId = 42L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Selecting)
        val lists = (state as SelectCheckInListUiState.Selecting).lists
        assertTrue(lists.any { it.id == 1L }, "Global list (subevent_id=null) should be included")
        assertTrue(lists.any { it.id == 2L }, "Matching subevent list should be included")
    }

    @Test
    fun `subevent-specific list is included when matching subevent ID`() = runTest {
        val subEventList = makeList(id = 1L, subEventId = 42L)
        stubLists(listOf(subEventList))

        val viewModel = createViewModel(subEventId = 42L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Selecting)
        assertEquals(1, (state as SelectCheckInListUiState.Selecting).lists.size)
        assertEquals(1L, state.lists.first().id)
    }

    @Test
    fun `subevent-specific list is excluded when subevent ID differs`() = runTest {
        val subEventList = makeList(id = 1L, subEventId = 99L)
        stubLists(listOf(subEventList))

        val viewModel = createViewModel(subEventId = 42L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Empty)
    }

    @Test
    fun `all lists are shown when no subevent is specified`() = runTest {
        val lists = listOf(
            makeList(id = 1L, subEventId = null),
            makeList(id = 2L, subEventId = 0L),
            makeList(id = 3L, subEventId = 42L),
            makeList(id = 4L, subEventId = 99L)
        )
        stubLists(lists)

        val viewModel = createViewModel(subEventId = null)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Selecting)
        assertEquals(4, (state as SelectCheckInListUiState.Selecting).lists.size)
    }

    @Test
    fun `mixed list is correctly filtered - global 0L, matching subevent, non-matching subevent`() = runTest {
        val globalList = makeList(id = 1L, subEventId = 0L)
        val matchingList = makeList(id = 2L, subEventId = 42L)
        val nonMatchingList = makeList(id = 3L, subEventId = 99L)
        stubLists(listOf(globalList, matchingList, nonMatchingList))

        val viewModel = createViewModel(subEventId = 42L)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SelectCheckInListUiState.Selecting)
        val result = (state as SelectCheckInListUiState.Selecting).lists
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == 1L })
        assertTrue(result.any { it.id == 2L })
    }
}
