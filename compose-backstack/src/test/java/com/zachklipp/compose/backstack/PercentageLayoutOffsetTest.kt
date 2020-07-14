@file:Suppress("DEPRECATION")

package com.zachklipp.compose.backstack

import androidx.ui.core.LayoutDirection
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.ipx
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import com.zachklipp.compose.backstack.IntPxPositionSubject.Companion.assertThat
import com.zachklipp.compose.backstack.TransitionDirection.Backward
import com.zachklipp.compose.backstack.TransitionDirection.Forward
import org.junit.Test

class PercentageLayoutOffsetTest {

    private val containerSize = IntPxSize(100.ipx, 100.ipx)

    private var isTop = false
    private var transitionDirection = Forward
    private var layoutDirection = LayoutDirection.Ltr

    @Test
    fun `modifies position when going backwards`() {
        isTop = false
        transitionDirection = Backward
        layoutDirection = LayoutDirection.Ltr
        assertThat(Slide.applyModifiedPosition(0f)).isEqualTo(-100, 0)
        assertThat(Slide.applyModifiedPosition(.25f)).isEqualTo(-75, 0)
        assertThat(Slide.applyModifiedPosition(.5f)).isEqualTo(-50, 0)
        assertThat(Slide.applyModifiedPosition(.75f)).isEqualTo(-25, 0)
        assertThat(Slide.applyModifiedPosition(1f)).isEqualTo(0, 0)
    }

    @Test
    fun `modifies position when top going forwards`() {
        isTop = true
        transitionDirection = Forward
        layoutDirection = LayoutDirection.Ltr
        assertThat(Slide.applyModifiedPosition(0f)).isEqualTo(100, 0)
        assertThat(Slide.applyModifiedPosition(.25f)).isEqualTo(75, 0)
        assertThat(Slide.applyModifiedPosition(.5f)).isEqualTo(50, 0)
        assertThat(Slide.applyModifiedPosition(.75f)).isEqualTo(25, 0)
        assertThat(Slide.applyModifiedPosition(1f)).isEqualTo(0, 0)
    }

    private fun BackstackTransition.applyModifiedPosition(
        visibility: Float
    ): IntPxPosition {
        val modifier = modifierForScreen(visibility, isTop) as PercentageLayoutOffset
        return modifier.offsetPosition(containerSize)
    }
}
