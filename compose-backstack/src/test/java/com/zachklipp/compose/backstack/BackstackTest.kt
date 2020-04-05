package com.zachklipp.compose.backstack

import androidx.ui.core.Modifier
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackstackTest {

    @Test
    fun `calculateRegularModifier handles single screen`() {
        assertScreenProperties(
            1, 1f,
            ScreenProperties(
                isVisible = true,
                modifier = Modifier.None
            )
        )
    }

    @Test
    fun `calculateRegularModifier handles two screens`() {
        val count = 2
        assertScreenProperties(
            count, 1f,
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = true,
                modifier = Modifier.None
            )
        )

        assertScreenProperties(
            count, .75f,
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .25f, isTop = false)
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .75f, isTop = true)
            )
        )

        assertScreenProperties(
            count, .25f,
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .75f, isTop = false)
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .25f, isTop = true)
            )
        )

        assertScreenProperties(
            count, 0f,
            ScreenProperties(
                isVisible = true,
                modifier = Modifier.None
            ),
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            )
        )
    }

    @Test
    fun `calculateRegularModifier handles three screens`() {
        val count = 3
        assertScreenProperties(
            count, 1f,
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = true,
                modifier = Modifier.None
            )
        )

        assertScreenProperties(
            count, .75f,
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .25f, isTop = false)
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .75f, isTop = true)
            )
        )

        assertScreenProperties(
            count, .25f,
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .75f, isTop = false)
            ),
            ScreenProperties(
                isVisible = true,
                modifier = TestModifier(visibility = .25f, isTop = true)
            )
        )

        assertScreenProperties(
            count, 0f,
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            ),
            ScreenProperties(
                isVisible = true,
                modifier = Modifier.None
            ),
            ScreenProperties(
                isVisible = false,
                modifier = HIDDEN_MODIFIER
            )
        )
    }

    private fun assertScreenProperties(
        count: Int,
        progress: Float,
        vararg expectedProperties: ScreenProperties
    ) {
        require(count > 0)
        require(expectedProperties.size == count)

        for (index in 0 until count) {
            val result = calculateRegularModifier(TestTransition, index, count, progress)
            assertThat(result).isEqualTo(expectedProperties[index])
        }
    }

    private object TestTransition : BackstackTransition {
        override fun modifierForScreen(
            visibility: Float,
            isTop: Boolean
        ): Modifier = TestModifier(visibility, isTop)
    }

    private data class TestModifier(val visibility: Float, val isTop: Boolean) : Modifier.Element
}
