package com.streamatico.polymarketviewer.ui.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WidgetOpenCoordinatorTest {
    @Test
    fun `issues open command for initial slug`() {
        val coordinator = WidgetOpenCoordinator()

        coordinator.onWidgetIntentSlug("event-x")

        val command = coordinator.openEventCommand.value
        assertNotNull(command)
        assertEquals("event-x", command?.eventSlug)
        assertEquals(1L, command?.requestId)
    }

    @Test
    fun `ignores duplicate slug when that event is already top destination`() {
        val coordinator = WidgetOpenCoordinator()
        coordinator.onWidgetIntentSlug("event-x")
        val firstCommand = coordinator.openEventCommand.value

        coordinator.onTopEventSlugChanged("event-x")
        coordinator.onWidgetIntentSlug("event-x")

        val secondCommand = coordinator.openEventCommand.value
        assertEquals(firstCommand, secondCommand)
    }

    @Test
    fun `reopens same slug after leaving event detail`() {
        val coordinator = WidgetOpenCoordinator()
        coordinator.onWidgetIntentSlug("event-x")
        val firstRequestId = coordinator.openEventCommand.value?.requestId ?: error("Expected first command")

        coordinator.onTopEventSlugChanged(null)
        coordinator.onWidgetIntentSlug("event-x")

        val secondCommand = coordinator.openEventCommand.value
        assertNotNull(secondCommand)
        assertEquals("event-x", secondCommand?.eventSlug)
        assertTrue((secondCommand?.requestId ?: 0L) > firstRequestId)
    }

    @Test
    fun `issues new command when switching from X to Y`() {
        val coordinator = WidgetOpenCoordinator()
        coordinator.onWidgetIntentSlug("event-x")
        coordinator.onTopEventSlugChanged("event-x")

        coordinator.onWidgetIntentSlug("event-y")

        val command = coordinator.openEventCommand.value
        assertNotNull(command)
        assertEquals("event-y", command?.eventSlug)
        assertEquals(2L, command?.requestId)
    }
}

