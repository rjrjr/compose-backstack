package com.zachklipp.compose.backstack

import androidx.animation.AnimationBuilder
import androidx.animation.TweenBuilder
import androidx.compose.*
import androidx.ui.animation.animatedFloat
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult.HideScreen
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult.ModifyScreen
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward

/** Defines the [AnimationBuilder] that [AnimatedBackstackTransition]s will use. */
val TransitionAnimationAmbient: Ambient<AnimationBuilder<Float>?> = ambientOf { null }

@Composable
private val currentTransitionAnimation: AnimationBuilder<Float>
    get() = TransitionAnimationAmbient.current ?: DefaultTransitionAnimation

@Composable
private val DefaultTransitionAnimation: AnimationBuilder<Float>
    get() {
        val context = ContextAmbient.current
        return TweenBuilder<Float>().apply {
            duration = context.resources.getInteger(android.R.integer.config_shortAnimTime)
        }
    }

/**
 * A [BackstackTransition] that animates transitions using the current [TransitionAnimationAmbient].
 *
 * See [modifierForScreen].
 */
abstract class AnimatedBackstackTransition : BackstackTransition() {
    @Composable
    final override fun transformScreen(
        isTop: Boolean,
        direction: TransitionDirection
    ): ScreenTransformResult {
        var finished by state { false }
        val animation = currentTransitionAnimation
        val animatingIn = (isTop && direction == Forward) || (!isTop && direction == Backward)

        // Unlock screens once they're finished animating out.
        if (finished && !animatingIn) return HideScreen

        // The second-to-top screen has the inverse visibility of the top screen.
        val visibility = animatedFloat(initVal = if (animatingIn) 0f else 1f)
        onCommit(animatingIn) {
            visibility.animateTo(
                targetValue = if (animatingIn) 1f else 0f,
                anim = animation,
                onEnd = { _, _ -> finished = true }
            )
        }
        return ModifyScreen(modifierForScreen(visibility.value, isTop))
    }

    /**
     * Returns a [Modifier] to use to draw screen in a [Backstack].
     *
     * @param visibility A float in the range `[0, 1]` that indicates at what visibility this screen
     * should be drawn. For example, this value will increase when [isTop] is true and the transition
     * is in the forward direction.
     * @param isTop True only when being called for the top screen. E.g. if the screen is partially
     * visible, then the top screen is always transitioning _out_, and non-top screens are either
     * transitioning out or invisible.
     */
    abstract fun modifierForScreen(
        visibility: Float,
        isTop: Boolean
    ): Modifier
}
