package eu.pretix.desktop.app.scan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.awt.Component
import java.awt.event.KeyEvent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GlobalScanHandlerTest {

    private val handler = GlobalScanHandler()
    private val source = object : Component() {}

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun teardown() {
        handler.dispose()
        Dispatchers.resetMain()
    }

    private fun typed(c: Char) = KeyEvent(source, KeyEvent.KEY_TYPED, 0L, 0, KeyEvent.VK_UNDEFINED, c)

    private fun type(text: String) = text.forEach { handler.handleKeyEvent(typed(it)) }

    @Test
    fun `Enter after barcode characters triggers a scan`() {
        type("x3fvqtq3yzny")

        assertTrue(handler.handleKeyEvent(typed('\n')))
    }

    @Test
    fun `Enter after discarded input does not trigger a scan`() {
        type("x3fvqtq3yzny")
        handler.discardTypedInput()

        assertFalse(handler.handleKeyEvent(typed('\n')))
    }
}
