package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme
import kotlin.math.abs

@Composable
internal fun ChangePercentIndicator(
    priceChangePercent: Int,
    modifier: Modifier = Modifier
) {
    val changeColor: Color
    val changeIcon: ImageVector

    when {
        priceChangePercent > 0 -> {
            changeColor = ExtendedTheme.colors.onTrendUpContainer
            changeIcon = ImageVector.vectorResource(R.drawable.ic_trend_up)
        }

        else -> {
            changeColor = ExtendedTheme.colors.onTrendDownContainer
            changeIcon = ImageVector.vectorResource(R.drawable.ic_trend_down)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Text("X")
        Icon(
            imageVector = changeIcon,
            contentDescription = null,
            tint = changeColor,
            modifier = Modifier
                .size(10.dp)
        )
        Spacer(Modifier.size(1.dp))
        Text(
            text = "${abs(priceChangePercent)}%",
            style = MaterialTheme.typography.labelLarge,
            color = changeColor,
        )
    }
}