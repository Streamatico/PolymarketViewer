package com.streamatico.polymarketviewer.ui.shared.components

import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun OpenInBrowserIconButton(
    url: String
) {
    val context = LocalContext.current

    IconButton(onClick = {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("OpenInPolymarketIconButton", "Failed to open URL: $url", e)
        }
    }) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.OpenInNew,
            contentDescription = "Open in browser"
        )
    }
}