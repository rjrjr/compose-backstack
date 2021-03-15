package com.zachklipp.compose.backstack.sample

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.zachklipp.compose.backstack.BackstackTransition
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import com.zachklipp.compose.backstack.modifierForScreen
import kotlin.math.pow

/**
 * Example of a custom transition that combines the existing [Crossfade] and [Slide] transitions
 * with some additional math and other transformations.
 */
object FancyTransition : BackstackTransition {
  override fun Modifier.modifierForScreen(
    visibility: State<Float>,
    isTop: Boolean
  ): Modifier = if (isTop) {
    // Start sliding in from the middle to reduce the motion a bit.
    val slideVisibility = derivedStateOf { lerp(.5f, 1f, visibility.value) }
    then(Slide.modifierForScreen(slideVisibility, isTop))
      .then(Crossfade.modifierForScreen(visibility, isTop))
  } else {
    // Move the non-top screen back, but only a little.
    val scaleVisibility = lerp(.9f, 1f, visibility.value)
    graphicsLayer(scaleX = scaleVisibility, scaleY = scaleVisibility)
      .then(Crossfade.modifierForScreen(derivedStateOf { visibility.value.pow(.5f) }, isTop))
  }
}
