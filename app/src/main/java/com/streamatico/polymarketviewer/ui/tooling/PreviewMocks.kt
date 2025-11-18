package com.streamatico.polymarketviewer.ui.tooling

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.streamatico.polymarketviewer.data.model.CommentCreatorProfileDto
import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.PositionDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.data.model.demoMarketDto
import com.streamatico.polymarketviewer.ui.event_detail.HierarchicalComment
import java.time.OffsetDateTime

internal object PreviewMocks {
    val sampleUserProfile = CommentCreatorProfileDto(
        name = "CryptoChad",
        pseudonym = "CryptoChad",
        profileImage = "https://via.placeholder.com/150/0000FF/808080?Text=User+Avatar",
        proxyWallet = "0x123abc456def7890",
        positions = listOf(
            PositionDto(
                tokenId = "token-yes-1",
                positionSize = "500000000" // 500 USDC
            ),
            PositionDto(
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
        profile = sampleUserProfile.copy(name = "BearishBob", proxyWallet = "0xabc123def456fed789"),
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
        groupItemTitle = "Will it close above $80k?",
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

    val sampleEvent = EventDto(
        id = "event-1",
        title = "Bitcoin Price Prediction End of Year",
        slug = "bitcoin-price-prediction-eoy",
        description = "Predict the price of Bitcoin by the end of the current year. This event covers multiple price targets.",
        category = "Crypto",
        imageUrl = "https://via.placeholder.com/600x300/0000FF/FFFFFF?Text=Event+Image",
        iconUrl = null,
        active = true,
        closed = false,
        volume = 800000.0, // Sum of market volumes
        liquidity = 35000.0, // Sum of market liquidities
        startDate = OffsetDateTime.now().minusDays(10),
        endDate = OffsetDateTime.now().plusDays(60),
        resolutionSource = "Multiple Exchanges Average",
        rawMarkets = listOf(
            sampleMarket1,
            sampleMarket2,
            sampleMarket3
        ),
        featured = true,
        featuredOrder = 1,
        tags = listOf(TagDto(id = "tag-crypto", label = "Crypto", slug = "crypto", forceShow = false))
    )

    // Mock ChartModelProducer for previews
    val previewChartModelProducer = CartesianChartModelProducer()
}

