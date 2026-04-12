package com.streamatico.polymarketviewer.ui.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class MarketBadgeStateTest {
    private val now: OffsetDateTime = OffsetDateTime.parse("2026-04-12T12:00:00Z")

    @Test
    fun `closed has highest priority`() {
        val state = MarketBadgeState.from(
            active = false,
            closed = true,
            endDate = now.minusSeconds(3600),
            currentTime = now
        )

        assertEquals(MarketBadgeState.Resolved, state)
    }

    @Test
    fun `inactive non-closed maps to locked`() {
        val state = MarketBadgeState.from(
            active = false,
            closed = false,
            endDate = now.plusSeconds(3600),
            currentTime = now
        )

        assertEquals(MarketBadgeState.Locked, state)
    }

    @Test
    fun `active with past end date maps to resolving`() {
        val state = MarketBadgeState.from(
            active = true,
            closed = false,
            endDate = now.minusSeconds(1),
            currentTime = now
        )

        assertEquals(MarketBadgeState.Resolving, state)
    }

    @Test
    fun `active with future end date maps to ends on date`() {
        val state = MarketBadgeState.from(
            active = true,
            closed = false,
            endDate = now.plusSeconds(30 * 60),
            currentTime = now
        )

        assertEquals(
            MarketBadgeState.ActiveEndsOnDate(endDate = now.plusSeconds(30 * 60)),
            state
        )
    }

    @Test
    fun `active without end date maps to unknown`() {
        val state = MarketBadgeState.from(
            active = true,
            closed = false,
            endDate = null,
            currentTime = now
        )

        assertEquals(MarketBadgeState.Unknown, state)
    }
}
