package com.streamatico.polymarketviewer.ui.tooling

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentCreatorProfileDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.ProfilePositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.gamma_api.demoEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.demoMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.demoOptimizedEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.demoOptimizedMarketDto
import com.streamatico.polymarketviewer.ui.event_detail.HierarchicalComment
import java.time.OffsetDateTime

internal object PreviewMocks {
    val sampleUserProfile = CommentCreatorProfileDto(
        name = "CryptoChad",
        pseudonym = "CryptoChad",
        profileImage = "https://via.placeholder.com/150/0000FF/808080?Text=User+Avatar",
        proxyWallet = "0x123abc456def7890",
        positions = listOf(
            ProfilePositionDto(
                tokenId = "token-yes-1",
                positionSize = "500000000" // 500 USDC
            ),
            ProfilePositionDto(
                tokenId = "token-no-2",
                positionSize = "1000000000" // 1000 USDC
            )
        ),
        displayUsernamePublic = true,
        bio = "Crypto enthusiast and trader.",
    )

    val sampleComment1 = CommentDto(
        id = "comment-1",
        body = "This looks promising! Going long here.",
        createdAt = OffsetDateTime.now().minusHours(2),
        profile = sampleUserProfile,
        parentCommentID = null,
        parentEntityID = null,
        parentEntityType = null,
        updatedAt = OffsetDateTime.now().minusMinutes(30),
        userAddress = null,
        replyAddress = null,
        reportCount = 10,
        reactionCount = 3,
    )

    val sampleReply1 = CommentDto(
        id = "reply-1",
        body = "Disagree, I think it's going down.",
        createdAt = OffsetDateTime.now().minusMinutes(30),
        profile = sampleUserProfile.copy(name = "BearishBob", proxyWallet = "0xabc123def456fed789", profileImage = "https://polymarket-upload.s3.us-east-2.amazonaws.com/fallback-image.png"),
        parentEntityType = null,
        parentEntityID = null,
        parentCommentID = null,
        reactionCount = 5,
        updatedAt = null,
        userAddress = null,
        replyAddress = null,
        reportCount = 0,
    )

    val sampleComment2 = CommentDto(
        id = "comment-2",
        body = "What does everyone think about the latest news?",
        createdAt = OffsetDateTime.now().minusDays(1),
        profile = sampleUserProfile.copy(name = "NewsNancy", proxyWallet = "0xfed789abc123def456", profileImage = "https://via.placeholder.com/150/FF0000/FFFFFF?Text=NN"),
        parentEntityType = null,
        parentEntityID = null,
        parentCommentID = null,
        reactionCount = 12,
        updatedAt = null,
        userAddress = null,
        replyAddress = null,
        reportCount = 2,
    )

    val sampleHierarchicalComments = listOf(
        HierarchicalComment(sampleComment1, listOf(sampleReply1)),
        HierarchicalComment(sampleComment2)
    )

    // Additional Comment Mocks
    val sampleHierarchicalCommentWithReply = HierarchicalComment(
        comment = sampleComment1,
        replies = listOf(sampleReply1)
    )

    val sampleHierarchicalCommentNoReply = HierarchicalComment(
        comment = sampleComment1.copy(
            id = "preview-comment-2",
            reactionCount = 0,
            body = "A shorter comment with no replies."
        ),
        replies = emptyList()
    )

    val sampleOutcomeMap = mapOf("token-yes-1" to "Yes", "token-no-2" to "No")
    val sampleTitleMap = mapOf("token-yes-1" to "Outcome Yes", "token-no-2" to "Outcome No")

    val sampleMarket1 = demoMarketDto(
        id = "market-1",
        groupItemTitle = "Will the price reach $100k?",
        slug = "price-100k",
        description = "Market for price prediction.",
        startDate = OffsetDateTime.now().minusDays(5),
        endDate = OffsetDateTime.now().plusDays(30),
        resolutionSource = "Coinbase",
        volume = 500000.0,
        liquidity = 20000.0,
        outcomesJson = "[\"Yes\", \"No\"]",
        outcomePricesJson = "[\"0.65\", \"0.35\"]",
        active = true,
        closed = false,
        oneDayPriceChange = 0.116,
    )

    val sampleMarket2 = demoMarketDto(
        id = "market-2",
        groupItemTitle = "Will it close above $90k?",
        slug = "price-90k",
        outcomesJson = "[\"Yes\", \"No\"]",
        outcomePricesJson = "[\"0.80\", \"0.20\"]",
        volume = 300000.0,
        liquidity = 15000.0,
        lastTradePrice = 0.79,
        bestBid = 0.78,
        bestAsk = 0.81
    )

    val sampleMarket3 = demoMarketDto(
        id = "market-3",
        groupItemTitle = "Will it close above $80k? This is a long text",
        slug = "price-80k",
        outcomesJson = "[\"Yes\", \"No\"]",
        outcomePricesJson = "[\"0.14\", \"0.25\"]",
        volume = 200000.0,
        liquidity = 10000.0,
        lastTradePrice = 0.14,
        active = true,
        closed = false,
        oneDayPriceChange = -0.05,
        umaResolutionStatus = "resolved"
    )

    val sampleMarket4LowPrice = demoMarketDto(
        id = "market-4",
        groupItemTitle = $$"Will it close above $XXX?",
        slug = "market-4-slug",
        outcomesJson = "[\"Yes\", \"No\"]",
        outcomePricesJson = "[\"0.003\", \"0.997\"]",
        volume = 123.0,
        liquidity = 10000.0,
        lastTradePrice = 0.14,
        active = true,
        closed = false,
        oneDayPriceChange = -0.05,
    )


    val sampleEvent1 = EventDto(
        id = "event-1",
        title = "Bitcoin Price Prediction End of Year",
        slug = "bitcoin-price-prediction-eoy",
        description = "This market will resolve according to the date (ET) Donald Trump first signs the Epstein Disclosure Bill, the bill passed by United States House of Representatives on November 18, into law.\n\nThe resolution source for this market will be a consensus of credible reporting.",
        category = "Crypto",
        imageUrl = "https://via.placeholder.com/600x300/0000FF/FFFFFF?Text=Event+Image",
        iconUrl = null,
        active = true,
        closed = false,
        volume = 800000.0, // Sum of market volumes
        liquidity = 35000.0, // Sum of market liquidities
        startDate = OffsetDateTime.parse("2023-08-01T07:22:00Z"),
        endDate = OffsetDateTime.parse("2024-09-01T23:55:00Z"),
        resolutionSource = "Multiple Exchanges Average",
        rawMarkets = listOf(
            sampleMarket1,
            sampleMarket2,
            sampleMarket3
        ),
        featured = true,
        featuredOrder = 1,
        tags = listOf(
            TagDto(id = "tag-crypto", label = "Crypto", slug = "crypto", forceShow = false),
            TagDto(id = "tag-prediction", label = "Prediction", slug = "prediction", forceShow = true),
            TagDto(id = "tag-end-of-year", label = "End of Year", slug = "end-of-year", forceShow = false),
            TagDto(id = "tag-multiple-exchanges", label = "Multiple Exchanges", slug = "multiple-exchanges", forceShow = false),
            TagDto(id = "tag-average", label = "Average", slug = "average", forceShow = false)
        )
    )

    // Mock ChartModelProducer for previews
    val previewChartModelProducer = CartesianChartModelProducer()

    // Additional Mocks for EventListItem
    val sampleBinaryEvent = demoEventDto(
        id = "event-bin",
        title = "Binary Event Example",
        slug = "event-bin-slug",
        description = "Event description for binary",
        category = "Test",
        imageUrl = "https://via.placeholder.com/150",
        iconUrl = null,
        resolutionSource = "Preview Source 1",
        active = true,
        closed = false,
        volume = 1000.0,
        liquidity = 50.0,
        featured = false,
        endDate = OffsetDateTime.now().plusHours(2),
        rawMarkets = listOf(sampleMarket1.copy(outcomesJson = "[\"Yes\", \"No\"]", outcomePricesJson = "[\"0.65\", \"0.35\"]"))
    )

    val sampleCategoricalEvent = demoOptimizedEventDto(
        id = "event-cat",
        title = "Game Award Winner",
        slug = "event-cat-slug",
        imageUrl = "https://via.placeholder.com/150/0000FF/FFFFFF?Text=Game",
        startDate = null,
        endDate = OffsetDateTime.now().plusDays(30),
        active = true,
        closed = false,
        rawMarkets = listOf(
            demoOptimizedMarketDto(
                slug = "m-cat-slug",
                active = true,
                closed = false,
            )
        )
    )

    val sampleMultiMarketOptimizedEvent = demoOptimizedEventDto(
        id = "event-multi",
        title = "Next Prime Minister of Canada after the election?",
        slug = "event-multi-slug",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Flag_of_Canada_%28Pantone%29.svg/160px-Flag_of_Canada_%28Pantone%29.svg.png",
        startDate = null,
        endDate = OffsetDateTime.now().minusDays(5),
        active = true,
        closed = true,
        rawMarkets = listOf(
            demoOptimizedMarketDto("Will Mark Carney be the next Canadian Prime Minister?", "m1-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.77, 0.23), groupItemTitle = "Mark Carney"),
            demoOptimizedMarketDto("Will Pierre Poilievre be the next Canadian Prime Minister?", "m2-slug", active = true, closed = true, listOf("Yes", "No"), listOf(0.24, 0.76), groupItemTitle = "Pierre Poilievre"),
            demoOptimizedMarketDto("Will Jagmeet Singh be the next Canadian Prime Minister?", "m3-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.01, 0.99), groupItemTitle = "Jagmeet Singh", umaResolutionStatus = "resolved"),
            demoOptimizedMarketDto("Will Someone Else be the next Canadian Prime Minister?", "m4-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.01, 0.99), groupItemTitle = "Someone Else"),
            //demoOptimizedMarketDto("Will Yet Another Candidate be the next Canadian Prime Minister?", "m5-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.00, 1.00), groupItemTitle = "Yet Another Candidate")
        )
    )

    val sampleOptimizedEvents = listOf(
        sampleMultiMarketOptimizedEvent.copy(id = "event-1", title = "Active event"),
        sampleMultiMarketOptimizedEvent.copy(id = "event-2", title = "Inactive event", active = false),
        sampleMultiMarketOptimizedEvent.copy(id = "event-3", title = "Inactive close event", active = false, closed = true),
    )
}
