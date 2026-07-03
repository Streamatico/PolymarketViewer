package com.streamatico.polymarketviewer.data.model.gamma_api

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EventDtoSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
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
}
