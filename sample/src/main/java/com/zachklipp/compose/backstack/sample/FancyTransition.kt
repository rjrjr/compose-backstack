package com.zachklipp.compose.backstack.sample

import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.util.lerp
import com.zachklipp.compose.backstack.BackstackTransition
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import kotlin.math.pow

/**
 * Example of a custom transition that combines the existing [Crossfade] and [Slide] transitions
 * with some additional math and other transformations.
 */
object FancyTransition : BackstackTransition {
    override fun modifierForScreen(
        visibility: Float,
        isTop: Boolean
    ): Modifier {
        return if (isTop) {
            // Start sliding in from the middle to reduce the motion a bit.
            val slideVisibility = lerp(.5f, 1f, visibility)
            Slide.modifierForScreen(slideVisibility, isTop)
                .then(Crossfade.modifierForScreen(visibility, isTop))
        } else {
            // Move the non-top screen back, but only a little.
            val scaleVisibility = lerp(.9f, 1f, visibility)
            Modifier.drawLayer(scaleX = scaleVisibility, scaleY = scaleVisibility)
                .then(Crossfade.modifierForScreen(visibility.pow(.5f), isTop))
        }
    }
}
