package com.zachklipp.compose.backstack.xray

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.DefaultCameraDistance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.zachklipp.compose.backstack.FrameController
import com.zachklipp.compose.backstack.FrameController.BackstackFrame
import com.zachklipp.compose.backstack.NoopFrameController
import kotlin.math.sin

/**
 * Returns a [FrameController] that wraps this [FrameController] and, when [enabled], displays all
 * the screens in the backstack in pseudo-3D space. The 3D stack can be navigated via touch
 * gestures.
 */
@Composable fun <T : Any> FrameController<T>.xrayed(enabled: Boolean): FrameController<T> =
  remember { XrayController<T>() }.also {
    it.enabled = enabled
    it.wrappedController = this
  }

private class XrayController<T : Any> : FrameController<T> {

  var enabled: Boolean by mutableStateOf(false)
  var wrappedController: FrameController<T> by mutableStateOf(NoopFrameController())

  private var offsetDpX by mutableStateOf(500.dp)
  private var offsetDpY by mutableStateOf(10.dp)
  private var rotationX by mutableStateOf(0f)
  private var rotationY by mutableStateOf(10f)
  private var scaleFactor by mutableStateOf(.5f)
  private var alpha by mutableStateOf(.4f)
  private var overlayAlpha by mutableStateOf(.2f)

  private var _activeFrames by mutableStateOf(emptyList<BackstackFrame<T>>())

  private val controlModifier = Modifier.pointerInput(Unit) {
    detectTransformGestures { _, pan, zoom, _ ->
      scaleFactor *= zoom
      // Dragging left-and-right rotates around the vertical Y axis.
      rotationY += pan.x / 5f
    }
  }

  // Use derivedStateOf to cache the mapped list.
  override val activeFrames by derivedStateOf {
    if (!enabled) wrappedController.activeFrames else {
      _activeFrames.mapIndexed { index, frame ->
        val modifier = Modifier.modifierForFrame(index, _activeFrames.size, 1f)
        return@mapIndexed frame.copy(modifier = modifier)
      }
    }
  }

  override fun updateBackstack(frames: List<BackstackFrame<T>>) {
    _activeFrames = frames
    wrappedController.updateBackstack(frames)
  }

  /**
   * Calculates a [Modifier] to apply to a screen when in inspection mode.
   *
   * The top screen will be drawn without any transformations.
   * All other screens will be drawn as a 3D stack.
   */
  private fun Modifier.modifierForFrame(
    frameIndex: Int,
    frameCount: Int,
    visibility: Float
  ): Modifier {
    // Draw the top screen as an overlay so it's clear where touch targets are. Once
    // compose supports transforming inputs as well as outputs, the top screen can
    // participate in scaling/rotation too.
    val isTop = frameIndex == frameCount - 1
    return graphicsLayer {
      // drawLayer will scale around the center of the bounds, so we need to offset relative
      // to that so the entire stack stays centered.
      val centerOffset =
        // Don't need to adjust the pivot point if there's only one screen.
        if (frameCount == 1) 0f
        // Add -1 + visibility so new screens animate "out of" the previous one.
        else (frameIndex - 1f + visibility) - frameCount / 3f

      val scale = if (isTop) 1f else scaleFactor
      scaleX = scale
      scaleY = scale

      translationX = (if (isTop) 0.dp else {
        // Adjust by screenCount to squeeze more in as the count increases.
        val densityFactor = 10f / frameCount
        // Adjust X offset by sin(rotation) so it looks 3D.
        val xRotation = sin(Math.toRadians(this@XrayController.rotationY.toDouble())).toFloat()
        (offsetDpX * centerOffset * scale * densityFactor * xRotation)
      }).toPx()
      translationY = (if (isTop) 0.dp else offsetDpY * centerOffset * scale).toPx()

      rotationX = if (isTop) 0f else this@XrayController.rotationX
      rotationY = if (isTop) 0f else this@XrayController.rotationY

      // This is the only transformation applied to the top screen, so it has some extra logic.
      alpha = when {
        // If there's only one screen in the stack, don't transform it at all.
        frameCount == 1 -> 1f
        isTop -> overlayAlpha
        else -> this@XrayController.alpha
        // Adjust alpha by visibility to make transition less jarring when adding/removing
        // screens.
      } * visibility

      cameraDistance = DefaultCameraDistance * 3
    }.then(
      // This is the top screen, it's the overlay and holds the control gesture.
      if (isTop) controlModifier else Modifier
    )
  }
}
