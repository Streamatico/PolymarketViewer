package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PositionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        modifier = modifier,
        content = content,
    )
}