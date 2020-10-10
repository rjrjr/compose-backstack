package com.zachklipp.compose.backstack

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.compose.ui.LayoutModifier
import androidx.compose.ui.Measurable
import androidx.compose.ui.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal class PercentageLayoutOffset(offset: Float) : LayoutModifier {
  private val offset = offset.coerceIn(-1f..1f)

  override fun MeasureScope.measure(
    measurable: Measurable,
    constraints: Constraints
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
  )

  override fun toString(): String = "${this::class.java.simpleName}(offset=$offset)"
}
