package eu.pretix.scan.tickets.data

import eu.pretix.libpretixsync.check.TicketCheckProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultStateMappingTest {

    private fun checkResult(
        type: TicketCheckProvider.CheckResult.Type,
        scanType: TicketCheckProvider.CheckInType = TicketCheckProvider.CheckInType.ENTRY,
        isCheckinAllowed: Boolean = false
    ): TicketCheckProvider.CheckResult {
        val result = TicketCheckProvider.CheckResult(type)
        result.scanType = scanType
        result.isCheckinAllowed = isCheckinAllowed
        return result
    }

    @Test
    fun `UNPAID with checkin allowed maps to DIALOG_UNPAID`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.UNPAID, isCheckinAllowed = true)
        assertEquals(ResultState.DIALOG_UNPAID, result.resultState())
    }

    @Test
    fun `UNPAID with checkin disallowed maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.UNPAID, isCheckinAllowed = false)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `VALID ENTRY maps to SUCCESS`() {
        val result = checkResult(
            TicketCheckProvider.CheckResult.Type.VALID,
            scanType = TicketCheckProvider.CheckInType.ENTRY
        )
        assertEquals(ResultState.SUCCESS, result.resultState())
    }

    @Test
    fun `VALID EXIT maps to SUCCESS_EXIT`() {
        val result = checkResult(
            TicketCheckProvider.CheckResult.Type.VALID,
            scanType = TicketCheckProvider.CheckInType.EXIT
        )
        assertEquals(ResultState.SUCCESS_EXIT, result.resultState())
    }

    @Test
    fun `USED maps to WARNING`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.USED)
        assertEquals(ResultState.WARNING, result.resultState())
    }

    @Test
    fun `ANSWERS_REQUIRED maps to DIALOG_QUESTIONS`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.ANSWERS_REQUIRED)
        assertEquals(ResultState.DIALOG_QUESTIONS, result.resultState())
    }

    @Test
    fun `INVALID maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.INVALID)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `BLOCKED maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.BLOCKED)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `CANCELED maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.CANCELED)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `REVOKED maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.REVOKED)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `ERROR type maps to ERROR state`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.ERROR)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `RULES maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.RULES)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `AMBIGUOUS maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.AMBIGUOUS)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `UNAPPROVED maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.UNAPPROVED)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `INVALID_TIME maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.INVALID_TIME)
        assertEquals(ResultState.ERROR, result.resultState())
    }

    @Test
    fun `PRODUCT maps to ERROR`() {
        val result = checkResult(TicketCheckProvider.CheckResult.Type.PRODUCT)
        assertEquals(ResultState.ERROR, result.resultState())
    }
}
