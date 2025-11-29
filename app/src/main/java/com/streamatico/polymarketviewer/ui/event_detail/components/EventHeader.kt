package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks

@Composable
fun EventHeader(
    event: EventDto,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Event title
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = event.title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Event image
        if (event.imageUrl != null) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Event description and tags
        val description = event.description?.trim()//event.description?.takeIf { it.isNotBlank() }

        if (description != null || !event.tags.isNullOrEmpty()) {
            var isDescriptionExpanded by remember { mutableStateOf(false) }
            var isDescriptionOverflowing by remember { mutableStateOf<Boolean?>(null) }

            Column {
                // Event description and tags
                Column(
                    modifier = Modifier
                        .let {
                            if (isDescriptionOverflowing != null) it.animateContentSize()
                            else it
                        }
                ) {
                    if (description != null) {
                        SelectionContainer {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                                overflow = TextOverflow.Ellipsis,
                                //modifier = Modifier.animateContentSize(),
                                onTextLayout = { layoutResult ->
                                    // Check if the text was cut (overflow)
                                    //if (!isDescriptionExpanded) {
                                        isDescriptionOverflowing = layoutResult.hasVisualOverflow
                                    //}
                                },
                            )
                        }
                    }

                    // Show tags if description is short (or empty) OR if it is expanded
                    if(isDescriptionOverflowing != null) {
                        if (isDescriptionOverflowing == false || isDescriptionExpanded) {
                            if (!event.tags.isNullOrEmpty()) {
                                EventTags(
                                    tags = event.tags,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                if(isDescriptionOverflowing == true || isDescriptionExpanded) {
                    val showMoreText = if (isDescriptionExpanded) "Show less" else "Show more"
                    Text(
                        text = showMoreText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    )
                }
            }
        }

        // Event properties
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            event.volume?.let {
                InfoRow(label = "Total Volume", value = UiFormatter.formatLargeValueUsd(it))
                Spacer(Modifier.height(8.dp))
            }
            event.category?.let {
                InfoRow(label = "Category", value = it)
                Spacer(Modifier.height(8.dp))
            }
            event.startDate?.let {
                InfoRow(label = "Starts", value = UiFormatter.formatDateTimeLong(it))
                Spacer(Modifier.height(8.dp))
            }
            event.endDate?.let {
                InfoRow(label = "Ends", value = UiFormatter.formatDateTimeLong(it))
                Spacer(Modifier.height(8.dp))
            }
            event.resolutionSource?.takeIf { it.isNotBlank() }?.let {
                InfoRow(label = "Resolution Source", value = it)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun EventHeaderPreview() {
    MaterialTheme {
        EventHeader(
            event = PreviewMocks.sampleEvent1,
            modifier = Modifier.padding(16.dp)
        )
    }
}

