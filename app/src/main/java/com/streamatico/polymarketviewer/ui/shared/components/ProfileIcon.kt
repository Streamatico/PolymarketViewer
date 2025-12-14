package com.streamatico.polymarketviewer.ui.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import kotlin.math.absoluteValue

@Composable
internal fun ProfileIcon(
    userAddress: String,
    iconUrl: String?,
    contentDescription: String?,
    avatarSize: Dp,
    onClick: (() -> Unit)? = null,
) {
    val modifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    if(iconUrl.isNullOrBlank() || iconUrl.contains("fallback-image")) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = contentDescription,
            tint = rememberAvatarTint(userAddress),
            modifier = modifier
                .size(avatarSize),
        )
    } else {
        AsyncImage(
            model = iconUrl,
            contentDescription = contentDescription,
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun rememberAvatarTint(userAddress: String?): Color {
    if (userAddress.isNullOrBlank()) {
        return LocalContentColor.current
    }

    val isDarkTheme = isSystemInDarkTheme()
    return remember(userAddress, isDarkTheme) {
        val hue = (userAddress.hashCode().absoluteValue % 360).toFloat()
        val saturation = 0.65f
        val lightness = if (isDarkTheme) 0.8f else 0.4f
        Color.hsl(hue, saturation, lightness)
    }
}