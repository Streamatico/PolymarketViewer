package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme
import kotlin.math.abs

@Composable
internal fun ChangePercentIndicator(
    priceChangePercent: Int
) {
    val changeColor: Color
    val changeIcon: ImageVector

    when {
        priceChangePercent > 0 -> {
            changeColor = ExtendedTheme.colors.onTrendUpContainer
            changeIcon = Icons.Default.ArrowDropUp
        }

        else -> {
            changeColor = ExtendedTheme.colors.onTrendDownContainer
            changeIcon = Icons.Default.ArrowDropDown
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = changeIcon,
            contentDescription = null,
            tint = changeColor,
            modifier = Modifier.size(24.dp)
                .offset(x = 4.dp)
        )
        Text(
            text = "${abs(priceChangePercent)}%",
            style = MaterialTheme.typography.labelLarge,
            color = changeColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}