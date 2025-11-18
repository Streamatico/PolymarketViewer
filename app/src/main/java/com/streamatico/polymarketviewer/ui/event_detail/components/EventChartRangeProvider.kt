package com.streamatico.polymarketviewer.ui.event_detail.components

import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlin.math.min

internal object EventChartRangeProvider : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 0.0

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        when {
            minY == 0.0 && maxY == 0.0 -> 1.0
            maxY <= 0.0 -> 0.0
            else -> {
                val count10 = (maxY+1).toInt() / 10
                min(count10 * 10 + 10, 100).toDouble()
            }
        }
}
