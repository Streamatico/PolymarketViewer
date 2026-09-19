package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.model.clob_api.PriceHistoryResponseDto
import com.streamatico.polymarketviewer.data.model.clob_api.SeriesDto
import com.streamatico.polymarketviewer.data.model.clob_api.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.data_api.LeaderBoardDto
import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTotalPositionValueDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTradedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentCreatorProfileDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDataDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDto
import com.streamatico.polymarketviewer.data.model.gamma_api.ProfilePositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultOptimizedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserAssociationDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

@OptIn(ExperimentalSerializationApi::class)
class PolymarketOptionalFieldsTest {
    // Reviewed application requirements, not the set of fields shown in an API example.
    // Exceptions to the server schema are explained in docs/polymarket-api-contract-audit.md.
    private val contracts: List<Pair<KSerializer<*>, Set<String>>> = listOf(
        SeriesDto.serializer() to setOf("id"),
        EventDto.serializer() to setOf("id", "title", "slug", "active", "closed", "markets"),
        OptimizedEventDto.serializer() to setOf("id", "title", "slug", "active", "closed", "markets"),
        MarketDto.serializer() to setOf("id", "question", "slug", "active", "closed", "archived"),
        OptimizedMarketDto.serializer() to setOf("question", "slug", "active", "closed", "archived", "outcomes", "outcomePrices"),
        TagDto.serializer() to setOf("id", "label", "slug"),
        CommentDto.serializer() to setOf("id", "profile"),
        CommentCreatorProfileDto.serializer() to setOf("proxyWallet"),
        ProfilePositionDto.serializer() to setOf("tokenId"),
        UserProfileDto.serializer() to setOf("proxyWallet"),
        UserAssociationDto.serializer() to emptySet(),
        SearchResultDto.serializer() to setOf("pagination"),
        SearchResultOptimizedDto.serializer() to setOf("hasMore"),
        PaginationDataDto.serializer(EventDto.serializer()) to setOf("data", "pagination"),
        PaginationDto.serializer() to setOf("hasMore", "totalResults"),
        UserPositionDto.serializer() to setOf("eventSlug"),
        UserClosedPositionDto.serializer() to setOf("outcome", "eventSlug", "avgPrice", "realizedPnl", "totalBought", "timestamp"),
        UserActivityDto.serializer() to setOf("timestamp", "asset", "title", "eventSlug", "outcome", "type", "size", "price"),
        LeaderBoardDto.serializer() to setOf("rank", "proxyWallet", "userName", "verifiedBadge"),
        UserTotalPositionValueDto.serializer() to setOf("user"),
        UserTradedDto.serializer() to setOf("user"),
        PriceHistoryResponseDto.serializer() to setOf("history"),
        TimeseriesPointDto.serializer() to setOf("t", "p"),
    )

    @TestFactory
    fun `DTOs require only reviewed fields`() = contracts.map { (serializer, required) ->
        val descriptor = serializer.descriptor
        DynamicTest.dynamicTest(descriptor.serialName.substringAfterLast('.')) {
            val actual = (0 until descriptor.elementsCount)
                .filterNot { descriptor.isElementOptional(it) }
                .map { descriptor.getElementName(it) }
                .toSet()

            assertEquals(required, actual, "Review new required fields against the upstream contract and UI needs")
            for (index in 0 until descriptor.elementsCount) {
                if (descriptor.getElementDescriptor(index).isNullable) {
                    assertTrue(
                        descriptor.isElementOptional(index),
                        "${descriptor.getElementName(index)} accepts null but still fails when absent; add a default"
                    )
                }
            }
        }
    }
}
