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
    fun `shouldAutoPrint returns false when autoPrintBadges is disabled`() {
        val position = JSONObject()
        assertFalse(shouldAutoPrint(false, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns false for non-SUCCESS result states`() {
        val position = JSONObject()
        assertFalse(shouldAutoPrint(true, ResultState.ERROR, position))
        assertFalse(shouldAutoPrint(true, ResultState.WARNING, position))
        assertFalse(shouldAutoPrint(true, ResultState.SUCCESS_EXIT, position))
        assertFalse(shouldAutoPrint(true, ResultState.LOADING, position))
    }

    @Test
    fun `shouldAutoPrint returns false when position is null`() {
        assertFalse(shouldAutoPrint(true, ResultState.SUCCESS, null))
    }

    @Test
    fun `shouldAutoPrint returns true for SUCCESS with no prior prints`() {
        val position = JSONObject()
        assertTrue(shouldAutoPrint(true, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns false for SUCCESS when already printed`() {
        val printLog = JSONObject()
            .put("successful", true)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertFalse(shouldAutoPrint(true, ResultState.SUCCESS, position))
    }

    @Test
    fun `shouldAutoPrint returns true for SUCCESS with only failed prior prints`() {
        val printLog = JSONObject()
            .put("successful", false)
            .put("type", "badge")
        val position = JSONObject().put("print_logs", JSONArray().put(printLog))
        assertTrue(shouldAutoPrint(true, ResultState.SUCCESS, position))
    }
}
