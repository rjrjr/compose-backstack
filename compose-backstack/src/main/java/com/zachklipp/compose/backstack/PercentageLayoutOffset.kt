package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.ui.core.*
import androidx.ui.unit.IntOffset
import androidx.ui.unit.IntSize

internal class PercentageLayoutOffset(offset: Float) : LayoutModifier {
    private val offset = offset.coerceIn(-1f..1f)

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
        layoutDirection: LayoutDirection
    ): MeasureScope.MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(offsetPosition(IntSize(placeable.width, placeable.height)))
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun offsetPosition(containerSize: IntSize) = IntOffset(
        // RTL is handled automatically by place.
        x = (containerSize.width * offset).toInt(),
        y = 0
    ).also { println("OMG layout out at $offset * ${containerSize.width}") }
}
