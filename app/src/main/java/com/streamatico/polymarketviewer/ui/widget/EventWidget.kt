package com.streamatico.polymarketviewer.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.core.os.ConfigurationCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.streamatico.polymarketviewer.MainActivity
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.domain.model.EventType
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.widget.components.WidgetHorizontalDivider
import com.streamatico.polymarketviewer.ui.widget.components.WidgetTitleBar
import com.streamatico.polymarketviewer.ui.widget.theme.PolymarketGlanceTheme
import com.streamatico.polymarketviewer.ui.widget.tooling.WidgetPreviewMocks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

internal class EventWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            PolymarketGlanceTheme {
                EventWidgetContent()
            }
        }
    }
}

internal class EventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED
        ) {
            EventWidgetGeneratedPreviewPublisher.publishIfSupported(context.applicationContext)
        }

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
    val size = LocalSize.current
    val preferences = currentState<Preferences>()

    EventWidgetContent(
        state = EventWidgetRenderState(
            size = size,
            selection = preferences.readSelection(),
            snapshot = preferences.readSnapshot(),
            disableInteractions = false
        )
    )
}

@Composable
private fun EventWidgetContent(state: EventWidgetRenderState) {
    val context = LocalContext.current
    //val size = state.size
    val size = LocalSize.current
    val sizeClass = resolveSizeClass(size.height)

    val selection = state.selection
    val snapshot = state.snapshot

    val rows = snapshot?.rows.orEmpty()
    val totalRowsCount = (snapshot?.totalRowsCount ?: 0).takeIf { it > 0 } ?: rows.size
    val progress = snapshot?.binaryYesPrice?.toFloat()
    val hasProgress = snapshot?.eventType == EventType.BinaryEvent && progress != null
    val hasHero = hasProgress

    val bitmap = snapshot?.imageCachePath?.let { path ->
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }

    val showFooter = shouldShowFooter(size.height)
    val maxRows = calculateRowLimit(
        height = size.height,
        hasProgress = hasProgress,
        hasHero = hasHero,
        showFooter = showFooter,
        sizeClass = sizeClass
    )
    val hasOverflow = rows.size > maxRows
    val reservedRows = if (hasOverflow) 1 else 0
    val visibleRows = rows.take((maxRows - reservedRows).coerceAtLeast(1))
    val remaining = max(totalRowsCount - visibleRows.size, 0)

    val title = snapshot?.eventTitle
        ?: selection?.eventTitle
        ?: context.getString(R.string.widget_select_event)
    val volumeText = UiFormatter.formatLargeValueUsd(snapshot?.volume ?: 0.0, suffix = " Vol.")
    val endDateText = snapshot?.let { formatEndDateLabel(context, it.endDateEpochMs, it.closed) }
    val updatedText = formatUpdatedLabel(context, snapshot?.updatedAtEpochMs)

    val clickAction = if (state.disableInteractions) {
        null
    } else {
        selection?.eventSlug?.let { actionStartActivity(createOpenEventIntent(context, it)) }
    }

    val themeColors = GlanceTheme.colors

    val rowTitleStyle = TextStyle(
        color = themeColors.onSurface,
        fontWeight = FontWeight.Medium
    )
    val primaryRowTitleStyle = TextStyle(
        color = themeColors.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    )
    val primaryValueStyle = TextStyle(
        color = themeColors.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    val secondaryValueStyle = TextStyle(
        color = themeColors.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
    val secondaryStyle = TextStyle(color = themeColors.onSurfaceVariant)

    val widgetBackgroundColor = themeColors.surface

    Scaffold(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .let { modifier ->
                if (clickAction != null) modifier.clickable(clickAction) else modifier
            },
        backgroundColor = widgetBackgroundColor,
        titleBar = {
            val startIcon = if (bitmap != null) ImageProvider(bitmap) else ImageProvider(R.drawable.ic_event_default)
            val refreshAction = actionRunCallback<EventWidgetRefreshAction>()

            WidgetTitleBar(
                startIcon = startIcon,
                startIconSize = IMAGE_WIDTH,
                title = title,
                actions = {
                    if (!state.disableInteractions) {
                        CircleIconButton(
                            onClick = refreshAction,
                            imageProvider = ImageProvider(R.drawable.ic_refresh),
                            contentDescription = null,
                            backgroundColor = widgetBackgroundColor,
                        )
                    }
                }
            )
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
                            Spacer(modifier = GlanceModifier.height(10.dp))
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
                            modifier = GlanceModifier
                                .fillMaxWidth(),
                            text = context.getString(R.string.widget_more_format, remaining),
                            style = TextStyle(
                                color = themeColors.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            if (showFooter) {
                WidgetHorizontalDivider()

                // Footer: Two rows for better information grouping
                Column {
                    // Row 1: Volume (left), [reserved space for future] (right)
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(text = volumeText, maxLines = 1, style = secondaryStyle)
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        // Reserved space for future info (e.g., status badge, participant count)

                        // DEBUG!!
                        //Text(text = sizeClass.toString(), maxLines = 1, style = secondaryStyle)
                    }

                    // Row 2: End Date (left), Updated time (right)
                    if (endDateText != null || updatedText != null) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            endDateText?.let {
                                Text(text = it, maxLines = 1, style = secondaryStyle)
                            }
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            updatedText?.let {
                                Text(text = it, maxLines = 1, style = secondaryStyle)
                            }
                        }
                    }
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
    showFooter: Boolean,
    sizeClass: WidgetSizeClass
): Int {
    val headerHeight = HEADER_HEIGHT
    val available = height -
        CONTENT_VERTICAL_PADDING -
        headerHeight -
        if (showFooter) FOOTER_HEIGHT else 0.dp -
        if (hasProgress) PROGRESS_SECTION_HEIGHT else 0.dp -
        if (hasHero) HERO_HEIGHT else 0.dp
    val rows = (max(0f, available.value) / ROW_SLOT_HEIGHT.value).toInt() - ROW_FIT_SAFETY_ROWS
    return rows.coerceIn(sizeClass.minRows, sizeClass.maxRows)
}

private fun shouldShowFooter(height: Dp): Boolean {
    return height >= FOOTER_MIN_HEIGHT
}

private fun resolveSizeClass(height: Dp): WidgetSizeClass {
    val value = height.value
    return when {
        //value < SMALL_HEIGHT_THRESHOLD -> WidgetSizeClass.Small
        value < MEDIUM_HEIGHT_THRESHOLD -> WidgetSizeClass.Medium
        else -> WidgetSizeClass.Large
    }
}

private fun formatUpdatedLabel(context: Context, updatedAtEpochMs: Long?): String? {
    if (updatedAtEpochMs == null) return null
    val instant = Instant.ofEpochMilli(updatedAtEpochMs)
    val duration = Duration.between(instant, Instant.now())
    val minutes = max(1, duration.toMinutes())

    val value = if (minutes < 60 * 24) {
        val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } else {
        "${minutes / (60 * 24)}d"
    }

    return context.getString(R.string.widget_updated_format, value)
}

private fun formatEndDateLabel(context: Context, endDateEpochMs: Long?, closed: Boolean): String? {
    if (endDateEpochMs == null) return null

    val endInstant = Instant.ofEpochMilli(endDateEpochMs)
    val endDateTime = endInstant.atZone(ZoneId.systemDefault())
    val now = Instant.now().atZone(ZoneId.systemDefault())
    val duration = Duration.between(now.toInstant(), endInstant)

    // If event already ended/closed
    if (closed || duration.isNegative) {
        val daysSinceEnd = kotlin.math.abs(duration.toDays())
        return when {
            daysSinceEnd == 0L -> "Ended today"
            daysSinceEnd == 1L -> "Ended yesterday"
            daysSinceEnd < 7 -> "Ended ${daysSinceEnd}d ago"
            else -> {
                val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()
                val formatter = DateTimeFormatter.ofPattern("MMM d", locale)
                "Ended ${endDateTime.format(formatter)}"
            }
        }
    }

    // Event is active - show remaining time or date
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        hours < 1 -> "Ends in <1h"
        hours < 24 -> "Ends in ${hours}h"
        days < 7 -> "Ends in ${days}d"
        else -> {
            val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()
            val formatter = if (endDateTime.year == now.year) {
                DateTimeFormatter.ofPattern("MMM d", locale)
            } else {
                DateTimeFormatter.ofPattern("MMM d, yyyy", locale)
            }
            "Ends ${endDateTime.format(formatter)}"
        }
    }
}

internal data class EventWidgetRenderState(
    val size: DpSize,
    val selection: EventWidgetSelection?,
    val snapshot: EventWidgetSnapshot?,
    val disableInteractions: Boolean
)


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 180)
@Composable
private fun EventWidgetContentBinaryPreview() {
    PolymarketGlanceTheme {
        EventWidgetContent(state = WidgetPreviewMocks.previewWidgetState(eventType = EventType.BinaryEvent))
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 180)
@Composable
private fun EventWidgetContentCategoricalPreview() {
    PolymarketGlanceTheme {
        EventWidgetContent(state = WidgetPreviewMocks.previewWidgetState(eventType = EventType.CategoricalMarket))
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 320, heightDp = 220)
@Composable
private fun EventWidgetContentMultiMarketPreview() {
    PolymarketGlanceTheme {
        EventWidgetContent(state = WidgetPreviewMocks.previewWidgetState(eventType = EventType.MultiMarket, size = DpSize(180.dp, 180.dp)))
    }
}

private val CONTENT_VERTICAL_PADDING = 16.dp
private val HEADER_HEIGHT = 40.dp
private val FOOTER_BOTTOM_PADDING = 8.dp
private val FOOTER_HEIGHT = (18.dp * 2) + 2.dp + FOOTER_BOTTOM_PADDING  // Two rows + spacer between + bottom padding
private val ROW_CONTENT_HEIGHT = 19.dp
private val PROGRESS_BAR_HEIGHT = 8.dp
private val PROGRESS_SECTION_HEIGHT = PROGRESS_BAR_HEIGHT + 10.dp  // Updated spacer from 6dp to 10dp
private val HERO_HEIGHT = 36.dp + 4.dp
private val IMAGE_WIDTH = 40.dp   // Changed from 56.dp to match in-app cards
private val FOOTER_MIN_HEIGHT = 112.dp
private val ROW_BOTTOM_PADDING = 5.dp  // Increased from 2.dp for better readability
private val ROW_SLOT_HEIGHT = ROW_CONTENT_HEIGHT + ROW_BOTTOM_PADDING
private const val ROW_FIT_SAFETY_ROWS = 1
// RemoteViews (Glance's rendering backend) limits direct children of any
// Column/Row to 10. Rows are chunked into groups of MAX_ROWS_PER_GROUP so
// each nested Column stays within that limit.
private const val MAX_ROWS_PER_GROUP = 8
//private const val SMALL_HEIGHT_THRESHOLD = 230f
private const val MEDIUM_HEIGHT_THRESHOLD = 360f

private enum class WidgetSizeClass(val minRows: Int, val maxRows: Int) {
//    Small(minRows = 3, maxRows = 8),
    Medium(minRows = 4, maxRows = 15),
    Large(minRows = 10, maxRows = 23)
}
