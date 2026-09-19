package com.streamatico.polymarketviewer.data.model.gamma_api

import com.streamatico.polymarketviewer.data.network.polymarketJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EventDtoSerializationTest {
    private val json = polymarketJson

    @Test
    fun `event reads the documented resolutionSource field`() {
        val event = json.decodeFromString<EventDto>(
            """{"id":"1","title":"AI bill?","slug":"ai-bill","active":true,
                "closed":false,"markets":[],"resolutionSource":"Official announcement"}"""
        )

        assertEquals("Official announcement", event.resolutionSource)
        assertEquals("ai-bill", event.slug)
    }

    @Test
    fun `event pagination decodes series without series type`() {
        val payload = """
            {
              "data": [
                {
                  "id": "191888",
                  "title": "SpaceX closing market cap by end of July?",
                  "slug": "spacex-closing-market-cap-by-end-of-july",
                  "active": true,
                  "closed": false,
                  "markets": [],
                  "series": [
                    {
                      "id": "12051",
                      "ticker": "spacex-closing-market-cap-end-of-month",
                      "slug": "spacex-closing-market-cap-end-of-month",
                      "title": "SpaceX Closing Market Cap End of Month",
                      "recurrence": "monthly",
                      "active": true,
                      "closed": false,
                      "archived": false,
                      "createdAt": "2026-06-30T21:38:27.445562Z",
                      "updatedAt": "2026-07-03T20:29:37.884584Z",
                      "commentCount": 0,
                      "requiresTranslation": false
                    }
                  ]
                }
              ],
              "pagination": {
                "hasMore": false,
                "totalResults": 1
              }
            }
        """.trimIndent()

        val page = json.decodeFromString<PaginationDataDto<EventDto>>(payload)
        val series = page.data.single().series?.single()

        assertEquals("12051", series?.id)
        assertNull(series?.seriesType)
    }

    @Test
    fun `AI Safety page decodes the live series without recurrence`() {
        val payload = requireNotNull(javaClass.getResource("/polymarket/ai-safety-missing-recurrence.json"))
            .readText()

        val page = json.decodeFromString<PaginationDataDto<EventDto>>(payload)
        val event = page.data.single()
        val series = requireNotNull(event.series).single()

        assertEquals("79075", event.id)
        assertEquals("10624", series.id)
        assertEquals("single", series.seriesType)
        assertNull(series.recurrence)
        assertEquals(7L, series.commentCount)
        assertEquals(1, page.pagination.totalResults)
    }

}
