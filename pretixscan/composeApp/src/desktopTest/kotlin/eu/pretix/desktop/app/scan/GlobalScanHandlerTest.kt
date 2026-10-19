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
import kotlin.test.assertEquals
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

    private fun pressed(keyCode: Int) = KeyEvent(source, KeyEvent.KEY_PRESSED, 0L, 0, keyCode, KeyEvent.CHAR_UNDEFINED)

    private fun released(keyCode: Int) = KeyEvent(source, KeyEvent.KEY_RELEASED, 0L, 0, keyCode, KeyEvent.CHAR_UNDEFINED)

    private fun type(text: String) = text.forEach { handler.handleKeyEvent(typed(it)) }

    private fun pressEnter() = listOf(
        handler.handleKeyEvent(pressed(KeyEvent.VK_ENTER)),
        handler.handleKeyEvent(typed('\n')),
        handler.handleKeyEvent(released(KeyEvent.VK_ENTER))
    )

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

    @Test
    fun `Enter completing a scan is swallowed on type and release`() {
        type("x3fvqtq3yzny")

        assertEquals(listOf(false, true, true), pressEnter())
        assertFalse(handler.handleKeyEvent(released(KeyEvent.VK_ENTER)))
    }

    @Test
    fun `Enter without buffered barcode passes through`() {
        assertEquals(listOf(false, false, false), pressEnter())
    }

    @Test
    fun `Enter after too short input passes through`() {
        type("ab1")

        assertEquals(listOf(false, false, false), pressEnter())
    }

    @Test
    fun `Enter handled by a focused search field passes through`() {
        type("x3fvqtq3yzny")

        assertFalse(handler.handleKeyEvent(pressed(KeyEvent.VK_ENTER)))
        handler.discardTypedInput()
        assertFalse(handler.handleKeyEvent(typed('\n')))
        assertFalse(handler.handleKeyEvent(released(KeyEvent.VK_ENTER)))
    }

    @Test
    fun `Non-Enter key press and release pass through after barcode characters`() {
        type("x3fvqtq3yzny")

        assertFalse(handler.handleKeyEvent(pressed(KeyEvent.VK_A)))
        assertFalse(handler.handleKeyEvent(released(KeyEvent.VK_A)))
    }

    @Test
    fun `Auto-repeated Enter press does not swallow a later Enter`() {
        type("x3fvqtq3yzny")

        assertFalse(handler.handleKeyEvent(pressed(KeyEvent.VK_ENTER)))
        assertTrue(handler.handleKeyEvent(typed('\n')))
        assertFalse(handler.handleKeyEvent(pressed(KeyEvent.VK_ENTER)))
        assertFalse(handler.handleKeyEvent(typed('\n')))
        assertFalse(handler.handleKeyEvent(released(KeyEvent.VK_ENTER)))

        assertEquals(listOf(false, false, false), pressEnter())
    }
}
