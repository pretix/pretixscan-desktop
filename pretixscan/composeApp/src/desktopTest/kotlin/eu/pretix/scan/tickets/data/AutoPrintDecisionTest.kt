package eu.pretix.scan.tickets.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoPrintDecisionTest {

    @Test
    fun `isPreviouslyPrinted returns false when position has no print_logs`() {
        val position = JSONObject()
        assertFalse(isPreviouslyPrinted(position))
    }

    @Test
    fun `isPreviouslyPrinted returns false when print_logs is empty`() {
        val position = JSONObject().put("print_logs", JSONArray())
        assertFalse(isPreviouslyPrinted(position))
    }

    @Test
    fun `isPreviouslyPrinted returns true when print_logs has successful badge entry`() {
        val printLog = JSONObject()
            .put("successful", true)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertTrue(isPreviouslyPrinted(position))
    }

    @Test
    fun `isPreviouslyPrinted returns false when print_logs has unsuccessful badge entry`() {
        val printLog = JSONObject()
            .put("successful", false)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertFalse(isPreviouslyPrinted(position))
    }

    @Test
    fun `isPreviouslyPrinted returns false when print_logs has successful non-badge entry`() {
        val printLog = JSONObject()
            .put("successful", true)
            .put("type", "ticket")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertFalse(isPreviouslyPrinted(position))
    }

    @Test
    fun `isPreviouslyPrinted returns true with mixed entries containing one successful badge`() {
        val logs = JSONArray()
            .put(JSONObject().put("successful", false).put("type", "badge"))
            .put(JSONObject().put("successful", true).put("type", "ticket"))
            .put(JSONObject().put("successful", true).put("type", "badge"))
        val position = JSONObject().put("print_logs", logs)
        assertTrue(isPreviouslyPrinted(position))
    }

    @Test
    fun `shouldAutoPrint returns false for WHEN_BUTTON_PRESSED even on SUCCESS with no prior prints`() {
        val position = JSONObject()
        assertFalse(shouldAutoPrint(BadgePrintPolicy.WHEN_BUTTON_PRESSED, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns false for non-SUCCESS result states`() {
        val position = JSONObject()
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.ERROR, position))
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.WARNING, position))
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.SUCCESS_EXIT, position))
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.LOADING, position))
    }

    @Test
    fun `shouldAutoPrint returns false when position is null`() {
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.SUCCESS, null))
    }

    @Test
    fun `shouldAutoPrint returns true for ONCE_IF_NOT_PRINTED on SUCCESS with no prior prints`() {
        val position = JSONObject()
        assertTrue(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns false for ONCE_IF_NOT_PRINTED on SUCCESS when already printed`() {
        val printLog = JSONObject()
            .put("successful", true)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns true for ONCE_IF_NOT_PRINTED on SUCCESS with only failed prior prints`() {
        val printLog = JSONObject()
            .put("successful", false)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertTrue(shouldAutoPrint(BadgePrintPolicy.ONCE_IF_NOT_PRINTED, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns true for ALWAYS on SUCCESS even when already printed`() {
        val printLog = JSONObject()
            .put("successful", true)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertTrue(shouldAutoPrint(BadgePrintPolicy.ALWAYS, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns false for ALWAYS on non-SUCCESS or null position`() {
        val position = JSONObject()
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ALWAYS, ResultState.ERROR, position))
        assertFalse(shouldAutoPrint(BadgePrintPolicy.ALWAYS, ResultState.SUCCESS, null))
    }
}
