package com.zachklipp.compose.backstack

import androidx.compose.Composable
import androidx.ui.core.LayoutDirection
import androidx.ui.core.LayoutModifier
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.unit.Density
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.ipx
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult.HideScreen
import com.zachklipp.compose.backstack.ScreenTransformer.ScreenTransformResult.ModifyScreen

/**
 * Defines transitions for a [Backstack]. Transitions control how screens are rendered by returning
 * [Modifier]s that will be used to wrap screen composables.
 *
 * Transitions that perform animation should subclass [AnimatedBackstackTransition] instead of
 * subclassing this directly.
 *
 * @see Slide
 * @see Crossfade
 */
abstract class BackstackTransition : ScreenTransformer {
    @Composable
    final override fun transformScreen(
        screenIndex: Int,
        screenCount: Int,
        direction: TransitionDirection?
    ): ScreenTransformResult {
        // When not transitioning, always just draw the top screen, and remove transitions from
        // the composition.
        if (direction == null) {
            return if (screenIndex == screenCount - 1) ModifyScreen(Modifier.None) else HideScreen
        }

        return when (screenIndex) {
            screenCount - 1,
            screenCount - 2 -> {
                // Important to keep this call in the same position in the composition so it can
                // retain state.
                transformScreen(
                    isTop = screenCount == screenIndex - 1,
                    direction = direction
                )
            }
            // All other screens should not be drawn at all. They're only kept around to maintain
            // their composable state.
            else -> HideScreen
        }
    }

    /**
     * Called only with the top and just-below-top screens when a transition is in progress.
     *
     * Screens involved in the transition will be "locked" to remain in the active stack until this
     * function returns [HideScreen] for them.
     */
    @Composable
    abstract fun transformScreen(
        isTop: Boolean,
        direction: TransitionDirection
    ): ScreenTransformResult

    /** A transition that completes immediately without animation. */
    object None : BackstackTransition() {
        @Composable
        override fun transformScreen(
            isTop: Boolean,
            direction: TransitionDirection
        ): ScreenTransformResult = if (isTop) ModifyScreen(Modifier.None) else HideScreen
    }

    /** A simple transition that slides screens horizontally. */
    object Slide : AnimatedBackstackTransition() {
        override fun modifierForScreen(
            visibility: Float,
            isTop: Boolean
        ): Modifier = PercentageLayoutOffset(
            offset = if (isTop) 1f - visibility else -1 + visibility
        )

        private class PercentageLayoutOffset(private val offset: Float) :
            LayoutModifier {
            override fun Density.modifyPosition(
                childSize: IntPxSize,
                containerSize: IntPxSize,
                layoutDirection: LayoutDirection
            ): IntPxPosition {
                var realOffset = offset.coerceIn(-1f..1f)
                if (layoutDirection == LayoutDirection.Rtl) realOffset *= -1f
                return IntPxPosition(
                    x = containerSize.width * realOffset,
                    y = 0.ipx
                )
            }
        }
    }

    /** A simple transition that crossfades between screens. */
    object Crossfade : AnimatedBackstackTransition() {
        override fun modifierForScreen(
            visibility: Float,
            isTop: Boolean
        ): Modifier = Modifier.drawOpacity(visibility)
    }
}
