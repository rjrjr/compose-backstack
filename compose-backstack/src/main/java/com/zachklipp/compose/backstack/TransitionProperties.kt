package com.zachklipp.compose.backstack

import androidx.animation.AnimatedFloat
import androidx.animation.AnimationBuilder
import androidx.animation.AnimationEndReason
import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.remember
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Modifier
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward

/**
 * Defines the [BackstackTransition] as well as animation properties and listeners for [Backstack]
 * to use to animate between screens.
 */
@Immutable
data class TransitionProperties<T : Any>(
    val transition: BackstackTransition,
    val animationBuilder: AnimationBuilder<Float>? = null,
    val onTransitionStarting: ((
        from: List<T>,
        to: List<T>,
        direction: TransitionDirection
    ) -> Unit)? = null,
    val onTransitionFinished: (() -> Unit)? = null
) : BackstackProcessor {

    /**
     * Animates between changes in the top key using this [transition].
     */
    @Composable
    override fun processStack(keys: List<Any>): List<ModifiedScreen<Any>> {
        val transitionProgress = animatedFloat(1f)
        val state = remember(transitionProgress) { TransitionState<T>(keys, transitionProgress) }
        state.properties = this
        state.updateKeys(keys, animationBuilder ?: DefaultBackstackAnimation)
        return calculateTransitionModifiers(state.visibleKeys, transition, transitionProgress.value)
    }
}

private fun calculateTransitionModifiers(
    visibleKeys: List<Any>,
    transition: BackstackTransition,
    progress: Float
): List<Pair<Any, Modifier>> {
    if (visibleKeys.size == 1) {
        return listOf(ModifiedScreen(visibleKeys[0], Modifier))
    }

    check(visibleKeys.size == 2)
    val (visibleUnder, visibleTop) = visibleKeys
    val topModifier = transition.modifierForScreen(progress, true)
    val underModifier = transition.modifierForScreen(1f - progress, false)

    return listOf(visibleUnder to underModifier, visibleTop to topModifier)
}

/**
 * @param oldKeys
 * When transitioning, contains a stable cache of the screens actually being displayed. Will not
 * change even if backstack changes during the transition.
 *
 * @param transitionProgress
 * Defines the progress of the current transition animation in terms of visibility of the top
 * screen. 1 means top screen is visible, 0 means top screen is entirely hidden. Must be 1 when
 * no transition in progress.
 */
private class TransitionState<T : Any>(
    private var oldKeys: List<Any>,
    private val transitionProgress: AnimatedFloat
) {
    lateinit var properties: TransitionProperties<T>

    /**
     * The part of the stack that is actually being shown. Must be of size 1 or 2.
     */
    var visibleKeys = listOf(oldKeys.last())
        private set

    /** Null means not transitioning. */
    private var direction: TransitionDirection? = null

    fun updateKeys(
        keys: List<Any>,
        animation: AnimationBuilder<Float>
    ) {
        val top = keys.last()

        if (direction == null && oldKeys != keys) {
            // Not in the middle of a transition and we got a new backstack.
            // This will also run after a transition, to clean up old keys out of the temporary backstack.
            val oldTop = oldKeys.last()

            if (top == oldTop) {
                // Don't need to transition, but some hidden keys changed to so we need to update the active
                // list to ensure hidden screens that no longer exist are torn down.
                @Suppress("UNUSED_VALUE")
                oldKeys = keys
            } else {
                // If the new top is in the old backstack, then it has probably already been seen, so the
                // navigation is logically backwards, even if the new backstack actually contains more
                // screens.
                direction = if (top in oldKeys) Backward else Forward

                if (direction == Backward) {
                    visibleKeys = listOf(top, oldTop)

                    // When going back the top screen needs to start off as visible.
                    transitionProgress.snapTo(1f)
                    transitionProgress.animateTo(
                        0f,
                        anim = animation,
                        onEnd = ::onTransitionEnd
                    )
                } else {
                    visibleKeys = listOf(oldTop, top)

                    // When going forward, the top screen needs to start off as invisible.
                    transitionProgress.snapTo(0f)
                    transitionProgress.animateTo(
                        1f,
                        anim = animation,
                        onEnd = ::onTransitionEnd
                    )
                }
                @Suppress("UNCHECKED_CAST")
                properties.onTransitionStarting?.invoke(
                    oldKeys as List<T>,
                    keys as List<T>,
                    direction!!
                )
                @Suppress("UNUSED_VALUE")
                oldKeys = keys
            }
        }
    }

    /** Callback passed to animations to cleanup after the transition is done. */
    private fun onTransitionEnd(
        reason: AnimationEndReason,
        @Suppress("UNUSED_PARAMETER") value: Float
    ) {
        if (reason == AnimationEndReason.TargetReached) {
            direction = null
            visibleKeys = listOf(oldKeys.last())
            transitionProgress.snapTo(1f)
            properties.onTransitionFinished?.invoke()
        }
    }
}
