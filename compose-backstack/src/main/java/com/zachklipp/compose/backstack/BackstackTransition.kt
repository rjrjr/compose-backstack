@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "DEPRECATION")

package com.zachklipp.compose.backstack

import android.annotation.SuppressLint
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide

/**
 * Defines transitions for a [Backstack]. Transitions control how screens are rendered by returning
 * [Modifier]s that will be used to wrap screen composables.
 *
 * @see Slide
 * @see Crossfade
 */
fun interface BackstackTransition {

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
  fun Modifier.modifierForScreen(
    visibility: State<Float>,
    isTop: Boolean
  ): Modifier

  /**
   * A simple transition that slides screens horizontally.
   */
  object Slide : BackstackTransition {
    override fun Modifier.modifierForScreen(
      visibility: State<Float>,
      isTop: Boolean
    ): Modifier = then(PercentageLayoutOffset(
      rawOffset = derivedStateOf { if (isTop) 1f - visibility.value else -1 + visibility.value }
    ))
  }

  /**
   * A simple transition that crossfades between screens.
   */
  object Crossfade : BackstackTransition {
    override fun Modifier.modifierForScreen(
      visibility: State<Float>,
      isTop: Boolean
    ): Modifier = alpha(visibility.value)
  }
}

/**
 * Convenience function to make it easier to make composition transitions.
 */
@SuppressLint("ModifierFactoryExtensionFunction")
fun BackstackTransition.modifierForScreen(
  visibility: State<Float>,
  isTop: Boolean
): Modifier = Modifier.modifierForScreen(visibility, isTop)
