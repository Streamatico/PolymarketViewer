package com.streamatico.polymarketviewer.ui.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height

@Composable
internal fun WidgetHorizontalDivider() {
    val verticalPadding = 4.dp

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Spacer(modifier = GlanceModifier.height(verticalPadding))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlanceTheme.colors.outline)
        ) {}
        Spacer(modifier = GlanceModifier.height(verticalPadding))
    }
}