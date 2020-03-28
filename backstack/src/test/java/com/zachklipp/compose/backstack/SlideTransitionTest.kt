package com.zachklipp.compose.backstack

import androidx.ui.core.LayoutDirection
import androidx.ui.core.LayoutModifier
import androidx.ui.unit.Density
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.ipx
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import com.zachklipp.compose.backstack.IntPxPositionSubject.Companion.assertThat
import org.junit.Test

class SlideTransitionTest {

    private val containerSize = IntPxSize(100.ipx, 100.ipx)

    @Test
    fun `modifies position when going backwards`() {
        val isTop = false
        assertThat(Slide.applyModifiedPosition(0f, containerSize, isTop))
            .isEqualTo(-100, 0)
        assertThat(Slide.applyModifiedPosition(.25f, containerSize, isTop))
            .isEqualTo(-75, 0)
        assertThat(Slide.applyModifiedPosition(.5f, containerSize, isTop))
            .isEqualTo(-50, 0)
        assertThat(Slide.applyModifiedPosition(.75f, containerSize, isTop))
            .isEqualTo(-25, 0)
        assertThat(Slide.applyModifiedPosition(1f, containerSize, isTop))
            .isEqualTo(0, 0)
    }

    @Test
    fun `modifies position when top going forwards`() {
        val isTop = true
        assertThat(Slide.applyModifiedPosition(0f, containerSize, isTop))
            .isEqualTo(100, 0)
        assertThat(Slide.applyModifiedPosition(.25f, containerSize, isTop))
            .isEqualTo(75, 0)
        assertThat(Slide.applyModifiedPosition(.5f, containerSize, isTop))
            .isEqualTo(50, 0)
        assertThat(Slide.applyModifiedPosition(.75f, containerSize, isTop))
            .isEqualTo(25, 0)
        assertThat(Slide.applyModifiedPosition(1f, containerSize, isTop))
            .isEqualTo(0, 0)
    }

    private fun BackstackTransition.applyModifiedPosition(
        visibility: Float,
        containerSize: IntPxSize,
        isTop: Boolean,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr
    ): IntPxPosition {
        val modifier = modifierForScreen(visibility, isTop) as LayoutModifier
        return modifier.modifyPosition(containerSize, layoutDirection)
    }

    private fun LayoutModifier.modifyPosition(
        containerSize: IntPxSize,
        layoutDirection: LayoutDirection
    ): IntPxPosition {
        val density = Density(1f)
        val childSize = IntPxSize(1.ipx, 1.ipx)
        return density.modifyPosition(childSize, containerSize, layoutDirection)
    }
}
