@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.compose.backstack

import androidx.compose.Composable
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.core.gesture.DragGestureDetector
import androidx.ui.core.gesture.DragObserver
import androidx.ui.core.gesture.ScaleGestureDetector
import androidx.ui.core.gesture.ScaleObserver
import androidx.ui.foundation.Box
import androidx.ui.unit.PxPosition

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
            override fun onDrag(dragDistance: PxPosition): PxPosition {
                if (!enabled) return PxPosition.Origin
                inspectionParams = inspectionParams.copy(
                    // Dragging left-and-right rotates around the vertical Y axis.
                    rotationYDegrees = inspectionParams.rotationYDegrees + (dragDistance.x.value / 5f)
                ).constrained()
                return dragDistance
            }
        }
    }

    val scaleDetector = ScaleGestureDetector(scaleObserver = scaleObserver)
    val drawDetector = DragGestureDetector(dragObserver = dragObserver)
    Box(modifier = scaleDetector + drawDetector) {
        children(inspectionParams.takeIf { enabled })
    }
}
