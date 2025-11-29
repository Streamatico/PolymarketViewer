package com.streamatico.polymarketviewer.ui.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme

@Composable
fun TrendText(
    isPositive: Boolean,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null
){
    val contentColor: Color
    val backgroundColor: Color

    if (isPositive) {
        contentColor = ExtendedTheme.colors.onTrendUpContainer
        backgroundColor = ExtendedTheme.colors.trendUpContainer
    } else {
        contentColor = ExtendedTheme.colors.onTrendDownContainer
        backgroundColor = ExtendedTheme.colors.trendDownContainer
    }

    Text(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 4.dp),
        text = text,
        style = style,
        color = contentColor,
        fontWeight = fontWeight,
    )
}