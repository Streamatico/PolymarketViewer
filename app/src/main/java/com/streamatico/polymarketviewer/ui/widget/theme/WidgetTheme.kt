package com.streamatico.polymarketviewer.ui.widget.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import com.streamatico.polymarketviewer.ui.theme.getPolymarketColorScheme

@Composable
fun PolymarketGlanceTheme(
    content: @GlanceComposable @Composable () -> Unit
) {
    val context = LocalContext.current

    GlanceTheme(
        colors = polymarketWidgetColorProviders(context),
        content = content
    )
}

private fun polymarketWidgetColorProviders(context: Context): ColorProviders =
    ColorProviders(
        light = getPolymarketColorScheme(context, false),
        dark = getPolymarketColorScheme(context, true)
    )


/*
internal object WidgetExtendedColors {
    val trendUpContainer = ColorProvider(TrendUpContainerLight, TrendUpContainerDark)
    val trendDownContainer = ColorProvider(TrendDownContainerLight, TrendDownContainerDark)
    val onTrendUpContainer = ColorProvider(OnTrendUpContainerAuto, OnTrendUpContainerAuto)
    val onTrendDownContainer = ColorProvider(OnTrendDownContainerAuto, OnTrendDownContainerAuto)
}
*/