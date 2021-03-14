@file:Suppress("UNUSED_PARAMETER")

package com.zachklipp.compose.backstack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Math.toRadians
import kotlin.math.sin

/**
 * Values used to control the display of the backstack in inspection mode.
 *
 * To keep the display usable, values will be constrained using the [constrained] function.
 *
 * @param offsetX The distance between screens along the x axis.
 * @param offsetY The distance between screens along the y axis.
 * @param rotationXDegrees The rotation in degrees around the x axis (rotates up and down).
 * Constrained to `(-90º, 90º)`.
 * @param rotationYDegrees The rotation in degrees around the y axis (rotates left and right).
 * Constrained to `(-90º, 90º)`.
 * @param scale The factor by which to scale all the screens behind the top one.
 * Constrained to `[0, 1]`.
 * @param opacity The alpha used to draw all the screens behind the top one.
 * Constrained to `[0, 1]`.
 * @param overlayOpacity The alpha used to draw the top screen, without any other transformations.
 * Constrained to `[0, 1]`.
 */
@Immutable
data class InspectionParams(
  val offsetX: Dp = 500.dp,
  val offsetY: Dp = 10.dp,
  val rotationXDegrees: Float = 0f,
  val rotationYDegrees: Float = 10f,
  val scale: Float = .5f,
  val opacity: Float = .4f,
  val overlayOpacity: Float = .2f
)

/** Constrain params to reasonable limits. */
fun InspectionParams.constrained() = InspectionParams(
    offsetX = offsetX.coerceIn(1.dp, 100.dp),
    offsetY = offsetY.coerceIn((-50).dp, 50.dp),
    rotationXDegrees = rotationXDegrees.coerceIn(-89f, 89f),
    rotationYDegrees = rotationYDegrees.coerceIn(-89f, 89f),
    scale = scale.coerceIn(0f, 1f),
    opacity = opacity.coerceIn(0f, 1f),
    overlayOpacity = overlayOpacity.coerceIn(0f, 1f)
)

internal class BackstackInspector(private val scope: CoroutineScope) {

  private val animation = FloatSpringSpec(stiffness = Spring.StiffnessLow)

  /**
   * True when the inspector is in control of rendering.
   * Will continue to return true after setting [params] to null until it's finished animating.
   */
  var isInspectionActive: Boolean by mutableStateOf(false)
    private set

  /**
   * Update the parameters used to display the rendering.
   *
   * Whenever new parameters are passed in, the display will animate towards them, and
   * [isInspectionActive] will immediately start returning true.
   *
   * When null is passed, the display will animate screens back to the default state, and
   * inspecting will start returning false _only after_ the default state is reached.
   */
  var params: InspectionParams? = null
    set(value) {
      val constrainedParams = value?.constrained()
      if ((field == null) != (constrainedParams == null)) {
        if (constrainedParams != null) {
          startInspecting()
        } else {
          stopInspecting()
        }
      }
      constrainedParams?.let {
        scope.launch { offsetDpX.animateTo(it.offsetX.value, animation) }
        scope.launch { offsetDpY.animateTo(it.offsetY.value, animation) }
        scope.launch { rotationX.animateTo(it.rotationXDegrees, animation) }
        scope.launch { rotationY.animateTo(it.rotationYDegrees, animation) }
        scope.launch { scaleFactor.animateTo(it.scale, animation) }
        scope.launch { alpha.animateTo(it.opacity, animation) }
        scope.launch { overlayAlpha.animateTo(it.overlayOpacity) }
      }
      field = constrainedParams
    }

  private val offsetDpX = Animatable(INITIAL_OFFSET_X)
  private val offsetDpY = Animatable(INITIAL_OFFSET_Y)
  private val rotationX = Animatable(INITIAL_ROTATION_X)
  private val rotationY = Animatable(INITIAL_ROTATION_Y)
  private val scaleFactor = Animatable(INITIAL_SCALE)
  private val alpha = Animatable(INITIAL_ALPHA)
  private val overlayAlpha = Animatable(INITIAL_OVERLAY_ALPHA)

  /**
   * Calculates a [Modifier] to apply to a screen when in inspection mode.
   *
   * The top screen will be drawn without the usual translations, and only use
   * [InspectionParams.overlayOpacity]. All other screens will be drawn as a 3D stack.
   * All transformations are animated.
   */
  @Suppress("ComposableModifierFactory", "ModifierFactoryExtensionFunction")
  @Composable
  internal fun inspectScreen(
    screenIndex: Int,
    screenCount: Int,
    visibility: Float
  ): Modifier {
    // Draw the top screen as an overlay so it's clear where touch targets are. Once
    // compose supports transforming inputs as well as outputs, the top screen can
    // participate in scaling/rotation too.
    val isTop = screenIndex == screenCount - 1
    val density = LocalDensity.current

    // drawLayer will scale around the center of the bounds, so we need to offset relative
    // to that so the entire stack stays centered.
    val centerOffset by animateFloatAsState(
      // Don't need to adjust the pivot point if there's only one screen.
      if (screenCount == 1) 0f
      // Add -1 + visibility so new screens animate "out of" the previous one.
      else (screenIndex - 1f + visibility) - screenCount / 3f
    )

    val scale by animateFloatAsState(if (isTop) 1f else scaleFactor.value)

    val offsetDpX by animateFloatAsState(
      if (isTop) 0f else {
        // Adjust by screenCount to squeeze more in as the count increases.
        val densityFactor = 10f / screenCount
        // Adjust X offset by sin(rotation) so it looks 3D.
        val xRotation = sin(toRadians(rotationY.value.toDouble())).toFloat()
        (centerOffset * offsetDpX.value * scale * densityFactor * xRotation)
      }
    )
    val offsetDpY by animateFloatAsState(if (isTop) 0f else (centerOffset * offsetDpY.value * scale))

    val rotationX by animateFloatAsState(if (isTop) 0f else (rotationX.value))
    val rotationY by animateFloatAsState(if (isTop) 0f else (rotationY.value))

    // This is the only transformation applied to the top screen, so it has some extra logic.
    val alpha by animateFloatAsState(
      when {
        // If there's only one screen in the stack, don't transform it at all.
        screenCount == 1 -> 1f
        isTop -> overlayAlpha.value
        else -> alpha.value
        // Adjust alpha by visibility to make transition less jarring when adding/removing
        // screens.
      } * visibility
    )

    return Modifier.graphicsLayer(
      scaleX = scale,
      scaleY = scale,
      rotationX = rotationX,
      rotationY = rotationY,
      translationX = with(density) { offsetDpX.dp.toPx() },
      translationY = with(density) { offsetDpY.dp.toPx() },
      alpha = alpha
    )
  }

  /** Transition to inspection mode. */
  private fun startInspecting() {
    isInspectionActive = true
  }

  /** Transition away from inspection mode. */
  private fun stopInspecting() {
    scope.launch {
      coroutineScope {
        launch { offsetDpX.animateTo(INITIAL_OFFSET_X, animation) }
        launch { offsetDpY.animateTo(INITIAL_OFFSET_Y, animation) }
        launch { rotationX.animateTo(INITIAL_ROTATION_X, animation) }
        launch { rotationY.animateTo(INITIAL_ROTATION_Y, animation) }
        launch { scaleFactor.animateTo(INITIAL_SCALE, animation) }
        launch { alpha.animateTo(INITIAL_ALPHA, animation) }
        launch { overlayAlpha.animateTo(INITIAL_OVERLAY_ALPHA, animation) }
      }
      // Once all the animations are done we need to tell the Backstack that we're done being in
      // control.
      isInspectionActive = false
    }
  }

  private companion object {
    // Values to use when the inspector is not active (inspecting is false).
    const val INITIAL_OFFSET_X = 0f
    const val INITIAL_OFFSET_Y = 0f
    const val INITIAL_ROTATION_X = 0f
    const val INITIAL_ROTATION_Y = 0f
    const val INITIAL_SCALE = 1f
    const val INITIAL_ALPHA = 1f
    const val INITIAL_OVERLAY_ALPHA = 1f
  }
}
