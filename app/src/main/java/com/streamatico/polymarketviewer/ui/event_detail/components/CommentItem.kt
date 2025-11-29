package com.streamatico.polymarketviewer.ui.event_detail.components

// --- Imports needed for Comment Composables --- //
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PolymarketUserProfile
import com.streamatico.polymarketviewer.data.model.gamma_api.ProfilePositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.getDisplayName
import com.streamatico.polymarketviewer.ui.event_detail.HierarchicalComment
import com.streamatico.polymarketviewer.ui.shared.ComposableUiFormatter
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import java.math.BigDecimal

// --- Helper data class for Position Badge --- //
private data class PositionBadgeData(
    val formattedSize: String?,
    val displayTitle: String?,
    val backgroundColor: Color,
    val contentColor: Color
)

// Define an enum for badge types (private to this file)
private enum class PositionBadgeType { YES, NO, DEFAULT }

// Make CommentItem internal so it can be called from EventDetailScreen
@Composable
internal fun CommentItem(
    hierarchicalComment: HierarchicalComment,
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>,
    isBinaryEvent: Boolean,
    onUserProfileClick: (userProfile: PolymarketUserProfile) -> Unit,
    indentation: Dp = 0.dp,
) {
    // Automatically expand if there is only one reply
    var isExpanded by remember { mutableStateOf(hierarchicalComment.replies.size == 1) }

    val comment = hierarchicalComment.comment
    val replies = hierarchicalComment.replies

    // --- Calculate badge data --- //
    val badgeData = rememberPositionBadgeData(comment, eventOutcomeTokensMap, eventTokenToGroupTitleMap, isBinaryEvent)

    Column(modifier = Modifier.padding(start = indentation, top = 12.dp, bottom = 12.dp, end = 8.dp)) {
        // --- Display common content --- //
        CommentContent(
            modifier = Modifier.fillMaxWidth(),
            comment = comment,
            badgeData = badgeData,
            avatarSize = 32.dp,
            onUserProfileClick = onUserProfileClick,
            showRepliesToggle = replies.isNotEmpty(),
            replyCount = replies.size,
            isExpanded = isExpanded,
            onToggleReplies = { isExpanded = !isExpanded }
        )

        // --- Display Replies Section --- //
        // Keep the reply display logic within CommentItem
        if (isExpanded && replies.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                replies.forEach { reply ->
                    ReplyItem(
                        comment = reply,
                        indentation = 16.dp,
                        // Pass token map to replies
                        eventOutcomeTokensMap = eventOutcomeTokensMap,
                        eventTokenToGroupTitleMap = eventTokenToGroupTitleMap,
                        isBinaryEvent = isBinaryEvent,
                        onUserProfileClick = onUserProfileClick // Pass callback down
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
// --- Composable helper to calculate badge data --- //
@Composable
private fun rememberPositionBadgeData(
    comment: CommentDto,
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>,
    isBinaryEvent: Boolean
): PositionBadgeData {
    val relevantPosition = remember(comment.profile?.positions, eventOutcomeTokensMap, eventTokenToGroupTitleMap) {
        comment.profile?.positions
            ?.filter { eventOutcomeTokensMap.containsKey(it.tokenId) }
            ?.maxByOrNull {
                try { BigDecimal(it.positionSize) } catch (_: Exception) { BigDecimal.ZERO }
            }
    }
    val positionOutcomeLabel = relevantPosition?.let { eventOutcomeTokensMap[it.tokenId] }
    val displayTitle = if(isBinaryEvent) {
        positionOutcomeLabel
    } else {
        relevantPosition?.let { eventTokenToGroupTitleMap[it.tokenId] }
    }
    val formattedPositionSize = relevantPosition?.let { UiFormatter.formatPositionSize(it.positionSize) }

    val badgeType = remember(positionOutcomeLabel) {
        when {
            positionOutcomeLabel?.contains("Yes", ignoreCase = true) == true -> PositionBadgeType.YES
            positionOutcomeLabel?.contains("No", ignoreCase = true) == true -> PositionBadgeType.NO
            else -> PositionBadgeType.DEFAULT
        }
    }

    val badgeBackgroundColor = when (badgeType) {
        PositionBadgeType.YES -> ExtendedTheme.colors.trendUpContainer
        PositionBadgeType.NO -> ExtendedTheme.colors.trendDownContainer
        PositionBadgeType.DEFAULT -> MaterialTheme.colorScheme.secondaryContainer
    }
    val badgeContentColor = when (badgeType) {
        PositionBadgeType.YES -> ExtendedTheme.colors.onTrendUpContainer
        PositionBadgeType.NO -> ExtendedTheme.colors.onTrendDownContainer
        PositionBadgeType.DEFAULT -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    return PositionBadgeData(
        formattedSize = formattedPositionSize,
        displayTitle = displayTitle,
        backgroundColor = badgeBackgroundColor,
        contentColor = badgeContentColor
    )
}

// --- Common Composable for Comment/Reply Content --- //
@Composable
private fun CommentContent(
    modifier: Modifier = Modifier,
    comment: CommentDto,
    badgeData: PositionBadgeData,
    avatarSize: Dp,
    onUserProfileClick: (userProfile: PolymarketUserProfile) -> Unit,
    showRepliesToggle: Boolean,
    replyCount: Int,
    isExpanded: Boolean,
    onToggleReplies: () -> Unit
) {
    val userProfile = comment.profile

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = userProfile?.profileImage,
                contentDescription = "${userProfile?.getDisplayName() ?: "Anon"}'s avatar",
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { userProfile?.also(onUserProfileClick) },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(if (avatarSize > 24.dp) 12.dp else 8.dp)) // Adjust spacing based on avatar
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) { // Row for name and position badge
                    Text(
                        userProfile?.getDisplayName() ?: "Anonymous",
                        fontWeight = if (avatarSize > 24.dp) FontWeight.Bold else FontWeight.Medium, // Adjust weight
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { userProfile?.also(onUserProfileClick) }
                    )
                    // Display position badge if found
                    if (badgeData.formattedSize != null && badgeData.displayTitle != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = badgeData.backgroundColor,
                            contentColor = badgeData.contentColor,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(color = badgeData.contentColor, fontWeight = FontWeight.Medium)) {
                                        append(badgeData.formattedSize)
                                    }
                                    append(" ")
                                    withStyle(style = SpanStyle(color = badgeData.contentColor)) {
                                        append(badgeData.displayTitle)
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 1.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) { // Row for timestamp and reactions
                    comment.createdAt?.let {
                        Text(
                            ComposableUiFormatter.formatRelativeTime(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    comment.reactionCount?.takeIf { it > 0 }?.let { count ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(if (avatarSize > 24.dp) 8.dp else 4.dp)) // Adjust spacing
        // Wrap comment body text in SelectionContainer
        SelectionContainer {
            Text(comment.body ?: "", style = MaterialTheme.typography.bodyMedium)
        }

        // --- Replies Toggle --- //
        if (showRepliesToggle && replyCount > 0) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onToggleReplies() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Hide replies" else "Show replies",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$replyCount ${if (replyCount == 1) "Reply" else "Replies"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ReplyItem(
    comment: CommentDto,
    indentation: Dp,
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>,
    isBinaryEvent: Boolean,
    onUserProfileClick: (userProfile: PolymarketUserProfile) -> Unit // Add callback parameter
) {
    // --- Calculate badge data --- //
    val badgeData = rememberPositionBadgeData(comment, eventOutcomeTokensMap, eventTokenToGroupTitleMap, isBinaryEvent)

    // --- Display common content using CommentContent --- //
    CommentContent(
        modifier = Modifier.padding(start = indentation),
        comment = comment,
        badgeData = badgeData,
        avatarSize = 24.dp,
        onUserProfileClick = onUserProfileClick,
        showRepliesToggle = false, // Replies don't have toggles
        replyCount = 0,
        isExpanded = false,
        onToggleReplies = { } // No-op
    )
}

// --- Previews for Comment Composables --- //

@Composable
private fun CommentItemPreviewTemplate(
    hierarchicalComment: HierarchicalComment,
) {
    MaterialTheme {
        CommentItem(
            hierarchicalComment = hierarchicalComment,
            eventOutcomeTokensMap = PreviewMocks.sampleOutcomeMap,
            eventTokenToGroupTitleMap = PreviewMocks.sampleTitleMap,
            isBinaryEvent = false,
            onUserProfileClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Comment With Reply")
@Composable
private fun CommentItemWithReplyPreview() {
    CommentItemPreviewTemplate(
        hierarchicalComment = PreviewMocks.sampleHierarchicalCommentWithReply,
    )
}

@Preview(showBackground = true, name = "Comment No Reply")
@Composable
private fun CommentItemNoReplyPreview() {
    CommentItemPreviewTemplate(
        hierarchicalComment = PreviewMocks.sampleHierarchicalCommentNoReply,
    )
}

@Preview(showBackground = true, name = "Reply Item Preview")
@Composable
private fun ReplyItemStandalonePreview() {
    // Previewing ReplyItem directly by calling CommentContent with reply parameters
    val badgeData = rememberPositionBadgeData(
        comment = PreviewMocks.sampleReply1.copy(profile = PreviewMocks.sampleUserProfile.copy(
            positions = listOf(ProfilePositionDto("token-no-2", "10000")) // Example 'No' position
        )),
        eventOutcomeTokensMap = PreviewMocks.sampleOutcomeMap,
        eventTokenToGroupTitleMap = PreviewMocks.sampleTitleMap,
        isBinaryEvent = true // Example binary context
    )
    MaterialTheme {
        CommentContent(
            modifier = Modifier.padding(start = 16.dp), // Typical reply indentation
            comment = PreviewMocks.sampleReply1,
            badgeData = badgeData,
            avatarSize = 24.dp,
            onUserProfileClick = { },
            showRepliesToggle = false,
            replyCount = 0,
            isExpanded = false,
            onToggleReplies = { }
        )
    }
}
