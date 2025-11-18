package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.data.model.EventType
import com.streamatico.polymarketviewer.domain.repository.CommentsSortOrder
import com.streamatico.polymarketviewer.ui.event_detail.HierarchicalComment
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.EventCommentsSection(
    displayableComments: List<HierarchicalComment>,
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    onRefreshComments: () -> Unit,
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>,
    eventType: EventType,
    holdersOnly: Boolean,
    commentsSortOrder: CommentsSortOrder,
    onToggleHoldersOnly: () -> Unit,
    onCommentsSortOrderChange: (CommentsSortOrder) -> Unit,
) {
    // Comments header
    item {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Comments", style = MaterialTheme.typography.titleLarge)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sort order dropdown (first, as on website)
                var sortDropdownExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = sortDropdownExpanded,
                    onExpandedChange = { sortDropdownExpanded = it }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    ) {
                        Text(
                            text = when (commentsSortOrder) {
                                CommentsSortOrder.NEWEST -> "Newest"
                                CommentsSortOrder.MOST_LIKED -> "Most liked"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort order",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = sortDropdownExpanded,
                        onDismissRequest = { sortDropdownExpanded = false },
                        modifier = Modifier.widthIn(min = 120.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest") },
                            onClick = {
                                onCommentsSortOrderChange(CommentsSortOrder.NEWEST)
                                sortDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Most liked") },
                            onClick = {
                                onCommentsSortOrderChange(CommentsSortOrder.MOST_LIKED)
                                sortDropdownExpanded = false
                            }
                        )
                    }
                }

                // Holders checkbox (second, as on website)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleHoldersOnly() }
                ) {
                    Checkbox(
                        checked = holdersOnly,
                        onCheckedChange = { onToggleHoldersOnly() }
                    )
                    Text("Holders", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        HorizontalDivider()
    }

    // Comments list
    if (displayableComments.isNotEmpty()) {
        items(displayableComments, key = { it.comment.id }) { hierarchicalComment ->
            CommentItem(
                hierarchicalComment = hierarchicalComment,
                eventOutcomeTokensMap = eventOutcomeTokensMap,
                eventTokenToGroupTitleMap = eventTokenToGroupTitleMap,
                onUserProfileClick = { profile ->
                    onNavigateToUserProfile(profile.proxyWallet)
                },
                isBinaryEvent = eventType == EventType.BinaryEvent,
            )
            HorizontalDivider()
        }
    }

    // Comments loading and error states
    item {
        if (commentsLoading && displayableComments.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (commentsError != null && displayableComments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error loading comments: $commentsError", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRefreshComments) { Text("Retry") }
            }
        } else if (!commentsLoading && displayableComments.isEmpty()) {
            Text(
                "No comments found.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventCommentsSectionPreview() {
    LazyColumn {
        EventCommentsSection(
            displayableComments = PreviewMocks.sampleHierarchicalComments,
            commentsLoading = false,
            commentsError = null,
            onNavigateToUserProfile = {},
            onRefreshComments = {},
            eventOutcomeTokensMap = emptyMap(),
            eventTokenToGroupTitleMap = emptyMap(),
            eventType = EventType.BinaryEvent,
            holdersOnly = false,
            commentsSortOrder = CommentsSortOrder.NEWEST,
            onToggleHoldersOnly = {},
            onCommentsSortOrderChange = {}
        )
    }
}

