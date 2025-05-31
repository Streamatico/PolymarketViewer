package com.streamatico.polymarketviewer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Define the structure for custom colors
@Immutable
data class ExtendedColors(
    val trendUpContainer: Color,
    val trendDownContainer: Color,

    val onTrendUpContainer: Color,
    val onTrendDownContainer: Color
)

// Define instances for light and dark themes
private val lightExtendedColors = ExtendedColors(
    trendUpContainer = TrendUpContainerLight,
    onTrendUpContainer = OnTrendUpContainerAuto,

    trendDownContainer = TrendDownContainerLight,
    onTrendDownContainer = OnTrendDownContainerAuto,
)

private val darkExtendedColors = ExtendedColors(
    trendUpContainer = TrendUpContainerDark,
    onTrendUpContainer = OnTrendUpContainerAuto,

    trendDownContainer = TrendDownContainerDark,
    onTrendDownContainer = OnTrendDownContainerAuto,
)

// CompositionLocal to provide extended colors
private val LocalExtendedColors = staticCompositionLocalOf { lightExtendedColors }


@Composable
fun PolymarketAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Determine which set of extended colors to use
    val extendedColors = if (darkTheme) darkExtendedColors else lightExtendedColors

    // Provide both MaterialTheme colors and ExtendedColors
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = baseColorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Custom theme object to access extended colors easily
object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

// Usage example (in another Composable):
// Text(
//     text = "Trending Up",
//     color = ExtendedTheme.colors.trendUp
// )
// Text(
//     text = "Trending Down",
//     color = ExtendedTheme.colors.trendDown
// )
