package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.ui.core.*
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.ipx

internal class PercentageLayoutOffset(offset: Float) : LayoutModifier {
    private val offset = offset.coerceIn(-1f..1f)

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
        layoutDirection: LayoutDirection
    ): MeasureScope.MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(offsetPosition(IntPxSize(placeable.width, placeable.height)))
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun offsetPosition(containerSize: IntPxSize) = IntPxPosition(
        // RTL is handled automatically by place.
        x = containerSize.width * offset,
        y = 0.ipx
    ).also { println("OMG layout out at $offset * ${containerSize.width}") }
}
