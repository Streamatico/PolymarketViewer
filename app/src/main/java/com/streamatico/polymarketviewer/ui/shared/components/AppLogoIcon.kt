package com.streamatico.polymarketviewer.ui.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.R

@Composable
fun AppLogoIcon (
    size: Dp = 64.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .background(
                    SolidColor(colorResource(R.color.app_brand_primary)),
                    RoundedCornerShape(8)
                )
        )

        // Foreground
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier.size(size)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    AppLogoIcon()
}