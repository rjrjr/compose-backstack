@file:Suppress("UNUSED_PARAMETER")

package com.zachklipp.compose.backstack

import androidx.animation.AnimationClockObservable
import androidx.animation.PhysicsBuilder
import androidx.animation.Spring
import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.mutableStateOf
import androidx.ui.animation.AnimatedFloatModel
import androidx.ui.animation.animate
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.core.drawLayer
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.vectormath.radians
import androidx.ui.graphics.withSave
import androidx.ui.unit.Density
import androidx.ui.unit.Dp
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
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

internal class BackstackInspector(clock: AnimationClockObservable) {

    private val animation = PhysicsBuilder<Float>(stiffness = Spring.StiffnessLow)

    /**
     * True when the inspector is in control of rendering.
     * Will continue to return true after setting [params] to null until it's finished animating.
     */
    @Composable
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
                offsetDpX.animateTo(it.offsetX.value, animation)
                offsetDpY.animateTo(it.offsetY.value, animation)
                rotationX.animateTo(it.rotationXDegrees, animation)
                rotationY.animateTo(it.rotationYDegrees, animation)
                scaleFactor.animateTo(it.scale, animation)
                alpha.animateTo(it.opacity, animation)
                overlayAlpha.animateTo(it.overlayOpacity)
            }
            field = constrainedParams
        }

    private val offsetDpX = AnimatedFloatModel(INITIAL_OFFSET_X, clock)
    private val offsetDpY = AnimatedFloatModel(INITIAL_OFFSET_Y, clock)
    private val rotationX = AnimatedFloatModel(INITIAL_ROTATION_X, clock)
    private val rotationY = AnimatedFloatModel(INITIAL_ROTATION_Y, clock)
    private val scaleFactor = AnimatedFloatModel(INITIAL_SCALE, clock)
    private val alpha = AnimatedFloatModel(INITIAL_ALPHA, clock)
    private val overlayAlpha = AnimatedFloatModel(INITIAL_OVERLAY_ALPHA, clock)

    /**
     * Calculates a [Modifier] to apply to a screen when in inspection mode.
     *
     * The top screen will be drawn without the usual translations, and only use
     * [InspectionParams.overlayOpacity]. All other screens will be drawn as a 3D stack.
     * All transformations are animated.
     */
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

        // drawLayer will scale around the center of the bounds, so we need to offset relative
        // to that so the entire stack stays centered.
        val centerOffset = animate(
            // Don't need to adjust the pivot point if there's only one screen.
            if (screenCount == 1) 0f
            // Add -1 + visibility so new screens animate "out of" the previous one.
            else (screenIndex - 1f + visibility) - screenCount / 3f
        )

        val scale = animate(if (isTop) 1f else scaleFactor.value)

        val offsetX = animate(
            if (isTop) 0f else {
                (centerOffset * offsetDpX.value * scale *
                        // Adjust by screenCount to squeeze more in as the count increases.
                        // Adjust X offset by sin(rotation) so it looks 3D.
                        (10f / screenCount) * sin(radians(rotationY.value)))
            }
        )
        val offsetY = animate(if (isTop) 0f else (centerOffset * offsetDpY.value * scale))

        val rotationX = animate(if (isTop) 0f else (rotationX.value))
        val rotationY = animate(if (isTop) 0f else (rotationY.value))

        // This is the only transformation applied to the top screen, so it has some extra logic.
        val alpha = animate(
            when {
                // If there's only one screen in the stack, don't transform it at all.
                screenCount == 1 -> 1f
                isTop -> overlayAlpha.value
                else -> alpha.value
                // Adjust alpha by visibility to make transition less jarring when adding/removing
                // screens.
            } * visibility
        )

        return createModifier(
            offsetX.dp, offsetY.dp,
            rotationX, rotationY,
            scale,
            alpha
        )
    }

    /** Transition to inspection mode. */
    private fun startInspecting() {
        isInspectionActive = true
    }

    /** Transition away from inspection mode. */
    private fun stopInspecting() {
        offsetDpX.animateTo(INITIAL_OFFSET_X, animation)
        offsetDpY.animateTo(INITIAL_OFFSET_Y, animation)
        rotationX.animateTo(INITIAL_ROTATION_X, animation)
        rotationY.animateTo(INITIAL_ROTATION_Y, animation)
        scaleFactor.animateTo(INITIAL_SCALE, animation)
        alpha.animateTo(INITIAL_ALPHA, animation)
        overlayAlpha.animateTo(INITIAL_OVERLAY_ALPHA, animation, onEnd = { _, _ ->
            // Doesn't matter which one, but we need to listen to the end of one of the animations
            // so we can tell the Backstack that we're done being in control.
            isInspectionActive = false
        })
    }

    private fun createModifier(
        offsetX: Dp,
        offsetY: Dp,
        rotationX: Float,
        rotationY: Float,
        scale: Float,
        alpha: Float
    ): Modifier {
        // If drawLayer ever gets translation support, this custom modifier can be deleted.
        val translateModifier = object : DrawModifier {
            override fun draw(
                density: Density,
                drawContent: () -> Unit,
                canvas: Canvas,
                size: PxSize
            ) {
                val offsetPxX = with(density) { offsetX.toPx().value }
                val offsetPxY = with(density) { offsetY.toPx().value }
                canvas.withSave {
                    canvas.translate(offsetPxX, offsetPxY)
                    drawContent()
                }
            }
        }
        val drawModifier = drawLayer(
            scaleX = scale,
            scaleY = scale,
            rotationX = rotationX,
            rotationY = rotationY,
            alpha = alpha
        )
        return translateModifier + drawModifier
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
