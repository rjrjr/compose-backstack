@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.gesture.DragObserver
import androidx.ui.core.gesture.ScaleObserver
import androidx.ui.core.gesture.dragGestureFilter
import androidx.ui.core.gesture.scaleGestureFilter
import androidx.ui.foundation.Box
import androidx.ui.geometry.Offset

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
    var inspectionParams: InspectionParams by state { InspectionParams() }

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
