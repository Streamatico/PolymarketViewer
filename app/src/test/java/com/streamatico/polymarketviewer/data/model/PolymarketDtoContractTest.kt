package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.model.clob_api.SeriesDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentCreatorProfileDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.ProfilePositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import com.streamatico.polymarketviewer.data.network.polymarketJson
import com.streamatico.polymarketviewer.domain.model.EventMarketsSortBy
import com.streamatico.polymarketviewer.domain.model.EventType
import com.streamatico.polymarketviewer.ui.shared.MarketDisplayRow
import com.streamatico.polymarketviewer.ui.shared.sortedByViewPriority
import com.streamatico.polymarketviewer.ui.shared.toCompactDisplayRows
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import com.streamatico.polymarketviewer.ui.shared.totalDisplayRowsCount
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class PolymarketDtoContractTest {
    @Test
    fun `series requires only its documented id`() {
        val series = polymarketJson.decodeFromString<SeriesDto>("""{"id":"10624"}""")

        assertEquals("10624", series.id)
        assertNull(series.ticker)
        assertNull(series.slug)
        assertNull(series.title)
        assertNull(series.recurrence)
        assertNull(series.active)
        assertNull(series.closed)
        assertNull(series.archived)
        assertNull(series.createdAt)
        assertNull(series.updatedAt)
        assertNull(series.commentCount)
    }

    @Test
    fun `series accepts explicit null metadata`() {
        val series = polymarketJson.decodeFromString<SeriesDto>(
            """{"id":"10624","ticker":null,"slug":null,"title":null,"seriesType":null,
                "recurrence":null,"active":null,"closed":null,"archived":null,
                "createdAt":null,"updatedAt":null,"commentCount":null}"""
        )

        assertEquals("10624", series.id)
        assertNull(series.recurrence)
        assertNull(series.seriesType)
        assertNull(series.active)
        assertNull(series.commentCount)
    }

    @Test
    fun `market missing auxiliary metadata preserves usable prices and status`() {
        val market = polymarketJson.decodeFromJsonElement<MarketDto>(marketPayload)

        assertEquals("market-1", market.id)
        assertTrue(market.active)
        assertFalse(market.closed)
        assertEquals(listOf("Yes", "No"), market.outcomes)
        assertEquals(listOf(0.7, 0.3), market.outcomePrices)
        assertNull(market.createdAt)
        assertNull(market.isNew)
        assertNull(market.isRestricted)
        assertNull(market.isReady)
        assertNull(market.isFunded)
        assertNull(market.isApproved)
        assertNull(market.manualActivation)
    }

    @Test
    fun `market accepts explicit null auxiliary metadata`() {
        val optional = listOf("createdAt", "new", "restricted", "ready", "funded", "approved", "manualActivation")
        val market = polymarketJson.decodeFromJsonElement<MarketDto>(
            JsonObject(marketPayload + optional.associateWith { JsonNull })
        )

        assertEquals(listOf(0.7, 0.3), market.outcomePrices)
        assertNull(market.createdAt)
        assertNull(market.isReady)
        assertNull(market.isRestricted)
        assertNull(market.manualActivation)
    }

    @Test
    fun `market retains supplied auxiliary metadata`() {
        val market = polymarketJson.decodeFromJsonElement<MarketDto>(
            JsonObject(marketPayload + polymarketJson.parseToJsonElement(
                """{"createdAt":"2026-09-19T12:00:00Z","new":true,"restricted":false,
                    "ready":true,"funded":false,"approved":true,"manualActivation":false}"""
            ).jsonObject)
        )

        assertEquals("2026-09-19T12:00Z", market.createdAt.toString())
        assertEquals(true, market.isNew)
        assertEquals(false, market.isRestricted)
        assertEquals(true, market.isReady)
        assertEquals(false, market.isFunded)
        assertEquals(true, market.isApproved)
        assertEquals(false, market.manualActivation)
    }

    @ParameterizedTest
    @CsvSource("0, 0.0", "10, 10.0", "2.5, 2.5", "-1.5, -1.5", "2147483648, 2147483648.0")
    fun `full and optimized markets accept numeric threshold strings`(raw: String, expected: Double) {
        val full = polymarketJson.decodeFromJsonElement<MarketDto>(
            JsonObject(marketPayload + ("groupItemThreshold" to JsonPrimitive(raw)))
        )
        val optimized = polymarketJson.decodeFromJsonElement<OptimizedMarketDto>(
            JsonObject(optimizedMarketPayload + ("groupItemThreshold" to JsonPrimitive(raw)))
        )

        assertEquals(expected, full.groupItemThreshold)
        assertEquals(expected, optimized.groupItemThreshold)
        assertEquals(listOf(0.7, 0.3), full.outcomePrices)
        assertEquals(listOf(0.7, 0.3), optimized.outcomePrices)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["other", "NaN", "Infinity", "1e999"])
    fun `unavailable or nonnumeric thresholds have no numeric sort key`(raw: String?) {
        val value = raw?.let { JsonPrimitive(it) } ?: JsonNull
        val full = polymarketJson.decodeFromJsonElement<MarketDto>(
            JsonObject(marketPayload + ("groupItemThreshold" to value))
        )
        val optimized = polymarketJson.decodeFromJsonElement<OptimizedMarketDto>(
            JsonObject(optimizedMarketPayload + ("groupItemThreshold" to value))
        )

        assertNull(full.groupItemThreshold)
        assertNull(optimized.groupItemThreshold)
        assertTrue(full.active)
        assertTrue(optimized.active)
    }

    @ParameterizedTest(name = "numeric threshold ordering, optimized={0}")
    @ValueSource(booleans = [false, true])
    fun `display priority orders numeric thresholds including fractions`(optimized: Boolean) {
        val markets: List<BaseMarketDto> = listOf("10", "2.5", "2").map { raw ->
            val payload = JsonObject(
                (if (optimized) optimizedMarketPayload else marketPayload) +
                    ("groupItemThreshold" to JsonPrimitive(raw))
            )
            if (optimized) polymarketJson.decodeFromJsonElement<OptimizedMarketDto>(payload)
            else polymarketJson.decodeFromJsonElement<MarketDto>(payload)
        }

        val sorted = markets.sortedByViewPriority(EventMarketsSortBy.None)

        assertEquals(listOf(2.0, 2.5, 10.0), sorted.map { it.groupItemThreshold })
        assertEquals(listOf(0.7, 0.7, 0.7), sorted.map { it.outcomePrices.first() })
    }

    @ParameterizedTest(name = "categorical main market in the middle, optimized={0}")
    @ValueSource(booleans = [false, true])
    fun `all display functions select the zero threshold market when it is not first`(optimized: Boolean) {
        val markets = listOf(
            categoricalMarketPayload("first", "-1", listOf("First A", "First B"), listOf(0.99, 0.01), optimized),
            categoricalMarketPayload(
                "main", "0", listOf("Draw", "Away", "Home", "Cancelled"), listOf(0.1, 0.25, 0.6, 0.05), optimized
            ),
            categoricalMarketPayload(
                "last", "1", listOf("Last A", "Last B", "Last C", "Last D", "Last E"),
                listOf(0.6, 0.1, 0.1, 0.1, 0.1), optimized
            ),
        )
        val payload = JsonObject(
            polymarketJson.parseToJsonElement(
                """{"id":"game-42","title":"Game winner","slug":"game-winner",
                    "active":true,"closed":false,"gameId":42}"""
            ).jsonObject + ("markets" to JsonArray(markets))
        )
        val event: BaseEventDto = if (optimized) {
            polymarketJson.decodeFromJsonElement<OptimizedEventDto>(payload)
        } else {
            polymarketJson.decodeFromJsonElement<EventDto>(payload)
        }

        assertEquals(EventType.CategoricalMarket, event.eventType)
        assertEquals(listOf(-1.0, 0.0, 1.0), event.baseMarkets.map { it.groupItemThreshold })
        assertEquals(
            listOf(
                MarketDisplayRow("Home", 0.6, null),
                MarketDisplayRow("Away", 0.25, null),
                MarketDisplayRow("Draw", 0.1, null),
                MarketDisplayRow("Cancelled", 0.05, null),
            ),
            event.toDisplayRows()
        )
        assertEquals(
            listOf(MarketDisplayRow("Home", 0.6, null), MarketDisplayRow("Away", 0.25, null)),
            event.toCompactDisplayRows(limit = 2)
        )
        assertEquals(4, event.totalDisplayRowsCount())
    }

    @Test
    fun `nested profile metadata may be absent or null`() {
        val profile = polymarketJson.decodeFromString<UserProfileDto>(
            """{"proxyWallet":"0x123","users":[{}, {"id":null,"creator":null,"mod":null}]}"""
        )
        val commentProfile = polymarketJson.decodeFromString<CommentCreatorProfileDto>(
            """{"proxyWallet":"0x123","positions":[{"tokenId":"123"}]}"""
        )
        val nullPosition = polymarketJson.decodeFromString<ProfilePositionDto>(
            """{"tokenId":"456","positionSize":null}"""
        )

        assertEquals("0x123", profile.proxyWallet)
        assertEquals(2, profile.users?.size)
        profile.users!!.forEach {
            assertNull(it.id)
            assertNull(it.creator)
            assertNull(it.mod)
        }
        assertNull(commentProfile.displayUsernamePublic)
        assertEquals("123", commentProfile.positions?.single()?.tokenId)
        assertNull(commentProfile.positions?.single()?.positionSize)
        assertNull(nullPosition.positionSize)
    }

    @Test
    fun `position does not require event id to retain navigation and value`() {
        val position = polymarketJson.decodeFromString<UserPositionDto>(
            """{"eventSlug":"ai-safety-bill","currentValue":123.45}"""
        )
        val nullId = polymarketJson.decodeFromString<UserPositionDto>(
            """{"eventId":null,"eventSlug":"ai-safety-bill","currentValue":123.45}"""
        )

        assertNull(position.eventId)
        assertNull(nullId.eventId)
        assertEquals("ai-safety-bill", position.eventSlug)
        assertEquals(123.45, position.value)
    }

    @Test
    fun `publishedAt metadata preserves strings without imposing a date format`() {
        val series = polymarketJson.decodeFromString<SeriesDto>("""{"id":"1","publishedAt":""}""")
        val tag = polymarketJson.decodeFromString<TagDto>(
            """{"id":"2","label":"AI","slug":"ai","publishedAt":"2026-09-19"}"""
        )

        assertEquals("", series.publishedAt)
        assertEquals("ai", tag.slug)
        assertEquals("2026-09-19", tag.publishedAt)
    }

    @Test
    fun `missing identity and malformed prices still fail instead of inventing data`() {
        assertThrows(SerializationException::class.java) {
            polymarketJson.decodeFromString<SeriesDto>("""{"recurrence":"monthly"}""")
        }
        assertThrows(SerializationException::class.java) {
            polymarketJson.decodeFromJsonElement<MarketDto>(JsonObject(marketPayload - "id"))
        }
        assertThrows(SerializationException::class.java) {
            polymarketJson.decodeFromJsonElement<MarketDto>(
                JsonObject(marketPayload + ("bestBid" to JsonPrimitive("invalid")))
            )
        }
    }

    private fun categoricalMarketPayload(
        id: String,
        threshold: String,
        outcomes: List<String>,
        prices: List<Double>,
        optimized: Boolean,
    ): JsonObject {
        val outcomeArray = JsonArray(outcomes.map { JsonPrimitive(it) })
        val priceArray = JsonArray(prices.map { JsonPrimitive(it.toString()) })
        return JsonObject(
            (if (optimized) optimizedMarketPayload else marketPayload) + mapOf(
                "id" to JsonPrimitive(id),
                "slug" to JsonPrimitive(id),
                "groupItemThreshold" to JsonPrimitive(threshold),
                "outcomes" to if (optimized) outcomeArray else JsonPrimitive(outcomeArray.toString()),
                "outcomePrices" to if (optimized) priceArray else JsonPrimitive(priceArray.toString()),
            )
        )
    }

    private val marketPayload = polymarketJson.parseToJsonElement(
        """{"id":"market-1","question":"AI bill?","slug":"ai-bill","active":true,
            "closed":false,"archived":false,"outcomes":"[\"Yes\",\"No\"]",
            "outcomePrices":"[\"0.7\",\"0.3\"]"}"""
    ).jsonObject

    private val optimizedMarketPayload = polymarketJson.parseToJsonElement(
        """{"question":"AI bill?","slug":"ai-bill","active":true,"closed":false,
            "archived":false,"outcomes":["Yes","No"],"outcomePrices":["0.7","0.3"]}"""
    ).jsonObject
}
