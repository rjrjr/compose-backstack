package com.zachklipp.compose.backstack.sample

import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.withSave
import androidx.ui.unit.Density
import androidx.ui.unit.PxSize
import com.zachklipp.compose.backstack.BackstackTransition
import kotlin.math.pow

internal object FancyTransition : BackstackTransition {
    override fun modifierForScreen(
        visibility: Float,
        isTop: Boolean
    ): Modifier {
        return if (isTop) {
            BackstackTransition.Slide.modifierForScreen(visibility.pow(1.1f), isTop) +
                    BackstackTransition.Crossfade.modifierForScreen(visibility.pow(.1f), isTop)
        } else {
            ScaleModifier(visibility.pow(.1f)) +
                    BackstackTransition.Crossfade.modifierForScreen(visibility.pow(.5f), isTop)
        }
    }

    private class ScaleModifier(private val factor: Float) :
        DrawModifier {
        override fun draw(
            density: Density,
            drawContent: () -> Unit,
            canvas: Canvas,
            size: PxSize
        ) {
            val halfWidth = size.width.value / 2
            val halfHeight = size.height.value / 2

            canvas.withSave {
                canvas.translate(halfWidth, halfHeight)
                canvas.scale(factor)
                canvas.translate(-halfWidth, -halfHeight)
                drawContent()
            }
        }
    }
}
