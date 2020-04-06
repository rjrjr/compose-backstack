package com.zachklipp.compose.backstack

import androidx.compose.Composable
import androidx.ui.core.Modifier
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult.HideScreen

/**
 * Defines a function, [transformScreen], that is applied to every screen in the active backstack
 * at all times.
 *
 * This can be used to define transition animations (see [BackstackTransition] and
 * [AnimatedBackstackTransition]), or other effects on the entire stack.
 *
 * Implementations of this class by doing two things:
 *
 * 1. Indicate whether the screen is actually going to be rendered. If it's not rendered, then the
 *    [Backstack] can clean up resources, including removing the screen's composable from the
 *    composition or removing old screens from the active stack.
 * 2. If the screen is to be shown, then specify a [Modifier] to apply to the screen's composable.
 *    This can be any modifier, and can perform virtually unlimited effects on the screen.
 */
interface ScreenTransformer {

    sealed class ScreenTransformResult {
        /**
         * When false, this screen's composable will be removed from the composition. If the screen
         * is only in the active stack temporarily, e.g. a top screen being navigated back from,
         * then it will also be removed from the active stack.
         */
        abstract val retainScreen: Boolean

        /**
         * The screen should be completely hidden. Returning this value will cause the screen to be
         * removed from the composition.
         */
        object HideScreen : ScreenTransformResult() {
            override val retainScreen: Boolean get() = false
        }

        /**
         * The screen should be shown with the given [Modifier].
         */
        data class ModifyScreen(
            val modifier: Modifier,
            override val retainScreen: Boolean = true
        ) : ScreenTransformResult()
    }

    /**
     * Called for each screen in a backstack.
     *
     * ## Lifecycle and Scoping
     *
     * This composable will remain in the composition, in a consistent position, as long as its
     * screen key remains in the active backstack. This means implementations can use Compose's
     * state mechanisms to store state over the lifetime of the screen, even when it's not being
     * shown.
     *
     * Note that [screenIndex] will change if the screen's key is moved around in the stack.ø
     *
     * ## Transitions
     *
     * If a transition is in progress, i.e. the backstack changed such that there is a new top,
     * [direction] will be non-null. During transition, screens that are involved in the transition
     * are considered "locked" until this function returns a [ScreenTransformResult.retainScreen]
     * value of false. This allows screens that have been removed from the backstack to be smoothly
     * transitioned away from.
     *
     * It is important to return a value with [ScreenTransformResult.retainScreen] set to false
     * (such as [HideScreen]) when not rendering a screen so that the [Backstack] can clean up
     * unused resources.
     */
    @Composable
    fun transformScreen(
        screenIndex: Int,
        screenCount: Int,
        direction: TransitionDirection?
    ): ScreenTransformResult
}
