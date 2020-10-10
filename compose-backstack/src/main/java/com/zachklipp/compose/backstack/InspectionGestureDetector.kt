@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack

import androidx.compose.foundation.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.DragObserver
import androidx.compose.ui.gesture.ScaleObserver
import androidx.compose.ui.gesture.dragGestureFilter
import androidx.compose.ui.gesture.scaleGestureFilter

/**
 * Wrap your [Backstack] with this composable to get convenient gesture-based control of the
 * inspector when [enabled] is true.
 *
 * ## Example
 *
 * ```
 * var inspectionEnabled by state { false }
 * InspectionGestureDetector(inspectionEnabled) { inspectionParams ->
 *   Backstack(
 *     backstack = …,
 *     inspectionParams = inspectionParams
 *   ) { screen -> … }
 * }
 * ```
 *
 * @param enabled When true, gestures will be intercepted and used to generate
 * [InspectionParams] passed to [children]. When false, [children] will always be passed
 * null.
 */
@Composable
fun InspectionGestureDetector(
  enabled: Boolean,
  children: @Composable() (InspectionParams?) -> Unit
) {
  var inspectionParams: InspectionParams by remember { mutableStateOf(InspectionParams()) }

  val scaleObserver = remember(enabled) {
    object : ScaleObserver {
      override fun onScale(scaleFactor: Float) {
        if (!enabled) return
        inspectionParams = inspectionParams.copy(
            scale = inspectionParams.scale * scaleFactor
        ).constrained()
      }
    }
  }
  val dragObserver = remember(enabled) {
    object : DragObserver {
      override fun onDrag(dragDistance: Offset): Offset {
        if (!enabled) return Offset.Zero
        inspectionParams = inspectionParams.copy(
            // Dragging left-and-right rotates around the vertical Y axis.
            rotationYDegrees = inspectionParams.rotationYDegrees + (dragDistance.x / 5f)
        ).constrained()
        return dragDistance
      }
    }
  }

  Box(
      modifier = Modifier
          .scaleGestureFilter(scaleObserver = scaleObserver)
          .dragGestureFilter(dragObserver = dragObserver)
  ) {
    children(inspectionParams.takeIf { enabled })
  }
}
