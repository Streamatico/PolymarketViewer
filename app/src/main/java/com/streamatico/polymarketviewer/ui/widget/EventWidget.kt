package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.streamatico.polymarketviewer.MainActivity
import com.streamatico.polymarketviewer.R
import java.time.Duration
import java.time.Instant
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class EventWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                EventWidgetContent()
            }
        }
    }
}

internal class EventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    EventWidget().updateAll(context.applicationContext)
                    EventWidgetUpdater.enqueueImmediate(context.applicationContext)
                }
            }
        }
    }
}

internal class EventWidgetRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        EventWidgetRefresher.refresh(context.applicationContext, glanceId)
    }
}

@Composable
private fun EventWidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    val sizeClass = resolveSizeClass(size.height)

    val preferences = currentState<Preferences>()
    val selection = preferences.readSelection()
    val snapshot = preferences.readSnapshot()

    val rows = snapshot?.rows.orEmpty()
    val totalRowsCount = (snapshot?.totalRowsCount ?: 0).takeIf { it > 0 } ?: rows.size
    val progress = snapshot?.binaryYesPrice?.toFloat()
    val hasProgress = snapshot?.eventType == "BinaryEvent" && progress != null
    val hasHero = hasProgress && sizeClass != WidgetSizeClass.Small

    val bitmap = if (sizeClass != WidgetSizeClass.Small) {
        snapshot?.imageCachePath?.let { path ->
            runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
        }
    } else null
    val showImage = bitmap != null

    val showFooter = shouldShowFooter(size.height)
    val maxRows = calculateRowLimit(size.height, hasProgress, hasHero, showImage, showFooter, sizeClass)
    val hasOverflow = rows.size > maxRows
    val reservedRows = if (hasOverflow) 2 else 0
    val visibleRows = rows.take((maxRows - reservedRows).coerceAtLeast(1))
    val remaining = max(totalRowsCount - visibleRows.size, 0)
    val trendState = resolveTrendState(rows, progress)

    val title = snapshot?.eventTitle
        ?: selection?.eventTitle
        ?: context.getString(R.string.widget_select_event)
    val statusText = snapshot?.let {
        if (it.closed) {
            context.getString(R.string.widget_status_closed)
        } else {
            context.getString(R.string.widget_status_open)
        }
    }
    val updatedText = formatUpdatedLabel(context, snapshot?.updatedAtEpochMs)

    val clickAction = selection?.eventSlug?.let {
        actionStartActivity(createOpenEventIntent(context, it))
    }
    val refreshAction = actionRunCallback<EventWidgetRefreshAction>()
    val themeColors = GlanceTheme.colors

    val trendIcon = when (trendState) {
        TrendState.Up -> R.drawable.ic_trend_up
        TrendState.Down -> R.drawable.ic_trend_down
        TrendState.Flat -> R.drawable.ic_trend_flat
    }
    val trendColor = when (trendState) {
        TrendState.Up -> themeColors.primary
        TrendState.Down -> themeColors.onSurface
        TrendState.Flat -> themeColors.onSurfaceVariant
    }

    val rowTitleStyle = TextStyle(
        color = themeColors.onSurface,
        fontWeight = FontWeight.Medium
    )
    val primaryRowTitleStyle = TextStyle(
        color = themeColors.onSurface,
        fontWeight = FontWeight.Bold
    )
    val primaryValueStyle = TextStyle(
        color = themeColors.primary,
        fontWeight = FontWeight.Bold
    )
    val secondaryValueStyle = TextStyle(
        color = themeColors.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
    val secondaryStyle = TextStyle(color = themeColors.onSurfaceVariant)

    Scaffold(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .let { modifier ->
                if (clickAction != null) modifier.clickable(clickAction) else modifier
            },
        backgroundColor = themeColors.surface,
        titleBar = {
            // Custom title bar replacing Glance's TitleBar component, which hardcodes maxLines=1.
            // Small widgets: single-line with ellipsis. Medium/Large: up to 2 lines.
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(trendIcon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(trendColor),
                    modifier = GlanceModifier.width(24.dp).height(24.dp)
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                Text(
                    text = title,
                    maxLines = if (sizeClass == WidgetSizeClass.Small) 1 else 2,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = themeColors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = context.getString(R.string.widget_refresh_cd),
                    colorFilter = ColorFilter.tint(themeColors.onSurfaceVariant),
                    modifier = GlanceModifier
                        .width(24.dp)
                        .height(24.dp)
                        .clickable(refreshAction)
                )
            }
        }
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                if (visibleRows.isEmpty()) {
                    val placeholderText = if (selection == null) {
                        context.getString(R.string.widget_select_event)
                    } else {
                        context.getString(R.string.widget_loading)
                    }
                    Text(text = placeholderText, style = secondaryStyle)
                } else {
                    // Wrap header elements (image + hero + progress) in a single Column
                    // so the outer defaultWeight Column's child count stays within the
                    // RemoteViews 10-child-per-container limit.
                    Column {
                        if (showImage) {
                            Image(
                                provider = ImageProvider(bitmap),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(IMAGE_HEIGHT)
                                    .cornerRadius(8.dp)
                            )
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }

                        if (hasHero) {
                            Text(
                                text = formatHeroPercent(progress),
                                style = TextStyle(
                                    color = themeColors.primary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }

                        if (hasProgress) {
                            LinearProgressIndicator(
                                progress = progress.coerceIn(0f, 1f),
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(PROGRESS_BAR_HEIGHT),
                                color = themeColors.primary,
                                backgroundColor = themeColors.surfaceVariant
                            )
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }

                    // Chunk rows into groups to respect RemoteViews' 10-child-per-container limit.
                    var overallIndex = 0
                    visibleRows.chunked(MAX_ROWS_PER_GROUP).fastForEach { chunk ->
                        Column {
                            chunk.fastForEach { row ->
                                val isFirst = overallIndex == 0
                                Row(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(bottom = ROW_BOTTOM_PADDING)
                                ) {
                                    Text(
                                        text = row.title,
                                        modifier = GlanceModifier.defaultWeight(),
                                        maxLines = 1,
                                        style = if (isFirst) primaryRowTitleStyle else rowTitleStyle
                                    )
                                    Spacer(modifier = GlanceModifier.width(8.dp))
                                    Text(
                                        text = row.value,
                                        maxLines = 1,
                                        style = if (isFirst) primaryValueStyle else secondaryValueStyle
                                    )
                                }
                                overallIndex++
                            }
                        }
                    }

                    if (remaining > 0) {
                        Text(
                            text = context.getString(R.string.widget_more_format, remaining),
                            style = TextStyle(
                                color = themeColors.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            if (showFooter && (statusText != null || updatedText != null)) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    statusText?.let { Text(text = it, maxLines = 1, style = secondaryStyle) }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    updatedText?.let { Text(text = it, maxLines = 1, style = secondaryStyle) }
                }
            }
            Spacer(modifier = GlanceModifier.height(FOOTER_BOTTOM_PADDING))
        }
    }
}

private fun createOpenEventIntent(context: Context, eventSlug: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(MainActivity.EXTRA_EVENT_SLUG, eventSlug)
    }

private fun formatHeroPercent(progress: Float): String {
    val percent = (progress * 100).roundToInt().coerceIn(0, 100)
    return "$percent%"
}

private fun calculateRowLimit(
    height: Dp,
    hasProgress: Boolean,
    hasHero: Boolean,
    showImage: Boolean,
    showFooter: Boolean,
    sizeClass: WidgetSizeClass
): Int {
    val headerHeight = if (sizeClass == WidgetSizeClass.Small) HEADER_HEIGHT_SINGLE else HEADER_HEIGHT_DOUBLE
    val available = height -
        CONTENT_VERTICAL_PADDING -
        headerHeight -
        if (showFooter) FOOTER_HEIGHT else 0.dp -
        if (showImage) IMAGE_SECTION_HEIGHT else 0.dp -
        if (hasProgress) PROGRESS_SECTION_HEIGHT else 0.dp -
        if (hasHero) HERO_HEIGHT else 0.dp
    val rows = (max(0f, available.value) / ROW_SLOT_HEIGHT.value).toInt() - ROW_FIT_SAFETY_ROWS
    return rows.coerceIn(sizeClass.minRows, sizeClass.maxRows)
}

private fun shouldShowFooter(height: Dp): Boolean {
    return height >= FOOTER_MIN_HEIGHT
}

private fun resolveTrendState(rows: List<EventWidgetRow>, progress: Float?): TrendState {
    val probability = progress ?: rows.firstNotNullOfOrNull { parseProbability(it.value) } ?: return TrendState.Flat
    return when {
        probability >= 0.55f -> TrendState.Up
        probability <= 0.45f -> TrendState.Down
        else -> TrendState.Flat
    }
}

private fun parseProbability(value: String): Float? {
    val trimmed = value.trim()
    if (trimmed == "--") return null
    if (trimmed.startsWith("<")) {
        return 0.005f
    }
    val percent = trimmed.removeSuffix("%").toFloatOrNull() ?: return null
    return (percent / 100f).coerceIn(0f, 1f)
}

private fun resolveSizeClass(height: Dp): WidgetSizeClass {
    val value = height.value
    return when {
        value < SMALL_HEIGHT_THRESHOLD -> WidgetSizeClass.Small
        value < MEDIUM_HEIGHT_THRESHOLD -> WidgetSizeClass.Medium
        else -> WidgetSizeClass.Large
    }
}

private fun formatUpdatedLabel(context: Context, updatedAtEpochMs: Long?): String? {
    if (updatedAtEpochMs == null) return null
    val duration = Duration.between(Instant.ofEpochMilli(updatedAtEpochMs), Instant.now())
    val minutes = max(1, duration.toMinutes())

    val value = when {
        minutes < 60 -> "${minutes}m"
        minutes < 60 * 24 -> "${minutes / 60}h"
        else -> "${minutes / (60 * 24)}d"
    }

    return context.getString(R.string.widget_updated_format, value)
}

private val CONTENT_VERTICAL_PADDING = 16.dp
private val HEADER_HEIGHT_SINGLE = 40.dp  // Small: single-line title
private val HEADER_HEIGHT_DOUBLE = 58.dp  // Medium/Large: up to 2-line title
private val FOOTER_BOTTOM_PADDING = 8.dp
private val FOOTER_HEIGHT = 18.dp + FOOTER_BOTTOM_PADDING
private val ROW_CONTENT_HEIGHT = 18.dp
private val PROGRESS_BAR_HEIGHT = 8.dp
private val PROGRESS_SECTION_HEIGHT = PROGRESS_BAR_HEIGHT + 6.dp
private val HERO_HEIGHT = 36.dp + 4.dp
private val IMAGE_HEIGHT = 56.dp
private val IMAGE_SECTION_HEIGHT = IMAGE_HEIGHT + 6.dp
private val FOOTER_MIN_HEIGHT = 112.dp
private val ROW_BOTTOM_PADDING = 2.dp
private val ROW_SLOT_HEIGHT = ROW_CONTENT_HEIGHT + ROW_BOTTOM_PADDING
private const val ROW_FIT_SAFETY_ROWS = 1
// RemoteViews (Glance's rendering backend) limits direct children of any
// Column/Row to 10. Rows are chunked into groups of MAX_ROWS_PER_GROUP so
// each nested Column stays within that limit.
private const val MAX_ROWS_PER_GROUP = 8
private const val SMALL_HEIGHT_THRESHOLD = 230f
private const val MEDIUM_HEIGHT_THRESHOLD = 360f

private enum class TrendState {
    Up,
    Down,
    Flat
}

private enum class WidgetSizeClass(val minRows: Int, val maxRows: Int) {
    Small(minRows = 3, maxRows = 8),
    Medium(minRows = 6, maxRows = 14),
    Large(minRows = 10, maxRows = 23)
}
