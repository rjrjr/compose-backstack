package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.compose.runtime.State
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal class PercentageLayoutOffset(private val rawOffset: State<Float>) : LayoutModifier {
  private val offset = { rawOffset.value.coerceIn(-1f..1f) }

  override fun MeasureScope.measure(
    measurable: Measurable,
    constraints: Constraints
  ): MeasureResult {
    val placeable = measurable.measure(constraints)
    return layout(placeable.width, placeable.height) {
      placeable.place(offsetPosition(IntSize(placeable.width, placeable.height)))
    }
  }

  @VisibleForTesting(otherwise = PRIVATE)
  internal fun offsetPosition(containerSize: IntSize) = IntOffset(
    // RTL is handled automatically by place.
    x = (containerSize.width * offset()).toInt(),
    y = 0
  )

  override fun toString(): String = "${this::class.java.simpleName}(offset=$offset)"
}
