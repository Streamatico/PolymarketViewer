package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.data.model.TagDto

@Composable
fun EventTags(
    tags: List<TagDto>,
    modifier: Modifier = Modifier,
    onTagClick: (TagDto) -> Unit = {} // Placeholder for future functionality
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.Center,
        //verticalArrangement = Arrangement.spacedBy(2.dp), // Add vertical spacing for wrapped items
        maxLines = 2,
    ) {
        tags.forEach { tag ->
            SuggestionChip(
                modifier = Modifier
                    .padding(horizontal = 2.dp),
                onClick = { onTagClick(tag) },
                label = {
                    Text(
                        text = tag.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}

