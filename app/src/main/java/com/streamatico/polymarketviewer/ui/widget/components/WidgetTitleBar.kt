package com.streamatico.polymarketviewer.ui.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
internal fun WidgetTitleBar(
    startIcon: ImageProvider,
    startIconSize: Dp,
    title: String,
    iconColor: ColorProvider? = GlanceTheme.colors.onSurface,
    textColor: ColorProvider = GlanceTheme.colors.onSurface,
    modifier: GlanceModifier = GlanceModifier,
    fontFamily: FontFamily? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    @Composable
    fun StartIcon() {
        val imagePadding = 8.dp

        Box(
            GlanceModifier
                .size(startIconSize + imagePadding * 2).padding(horizontal = imagePadding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = GlanceModifier.size(startIconSize),
                provider = startIcon,
                contentDescription = "",
                //contentScale = ContentScale.Fit,
                //colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
        }
    }

    @Composable
    fun RowScope.Title() {
        Text(
            text = title,
            style = TextStyle(
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                fontFamily = fontFamily
            ),
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight()
        )
    }

    Row(
        modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        StartIcon()
        Title()
        actions()
    }
}