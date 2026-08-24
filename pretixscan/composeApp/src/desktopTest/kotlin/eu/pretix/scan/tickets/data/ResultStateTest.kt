package eu.pretix.scan.tickets.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ResultStateTest {

    @Test
    fun `EMPTY has Transient dismiss behavior`() {
        assertEquals(DismissBehavior.Transient, ResultState.EMPTY.dismissBehavior())
    }

    @Test
    fun `LOADING has Transient dismiss behavior`() {
        assertEquals(DismissBehavior.Transient, ResultState.LOADING.dismissBehavior())
    }

    @Test
    fun `SUCCESS has AutoDismiss behavior`() {
        assertEquals(DismissBehavior.AutoDismiss, ResultState.SUCCESS.dismissBehavior())
    }

    @Test
    fun `SUCCESS_EXIT has AutoDismiss behavior`() {
        assertEquals(DismissBehavior.AutoDismiss, ResultState.SUCCESS_EXIT.dismissBehavior())
    }

    @Test
    fun `ERROR has AutoDismiss behavior`() {
        assertEquals(DismissBehavior.AutoDismiss, ResultState.ERROR.dismissBehavior())
    }

    @Test
    fun `WARNING has AutoDismiss behavior`() {
        assertEquals(DismissBehavior.AutoDismiss, ResultState.WARNING.dismissBehavior())
    }

    @Test
    fun `DIALOG_UNPAID requires user interaction`() {
        assertEquals(DismissBehavior.RequiresUserInteraction, ResultState.DIALOG_UNPAID.dismissBehavior())
    }

    @Test
    fun `DIALOG_QUESTIONS requires user interaction`() {
        assertEquals(DismissBehavior.RequiresUserInteraction, ResultState.DIALOG_QUESTIONS.dismissBehavior())
    }

    @Test
    fun `all ResultState entries have a dismiss behavior`() {
        ResultState.entries.forEach { state ->
            assertNotNull(state.dismissBehavior(), "Missing dismiss behavior for $state")
        }
    }
}
