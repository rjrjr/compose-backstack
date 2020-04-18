package com.zachklipp.compose.backstack

import androidx.animation.ManualAnimationClock
import androidx.animation.TweenBuilder
import androidx.compose.Model
import androidx.compose.Providers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.foundation.Text
import androidx.ui.test.*
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Model
private class State(
    var backstack: List<String>
)

@RunWith(AndroidJUnit4::class)
class BackstackComposableTest {

    @get:Rule
    val compose = createComposeRule()

    private val clock = ManualAnimationClock(0)
    private val animation = TweenBuilder<Float>().apply { duration = 100 }

    @Test
    fun initialStateWithSingleScreen_slide() {
        assertInitialStateWithSingleScreen(Slide)
    }

    @Test
    fun initialStateWithSingleScreen_crossfade() {
        assertInitialStateWithSingleScreen(Crossfade)
    }

    @Test
    fun initialStateWithMultipleScreens_slide() {
        assertInitialStateWithMultipleScreens(Slide)
    }

    @Test
    fun initialStateWithMultipleScreens_crossfade() {
        assertInitialStateWithMultipleScreens(Crossfade)
    }

    @Test
    fun transition_slide() {
        assertTransition(Slide)
    }

    @Test
    fun transition_crossfade() {
        assertTransition(Crossfade)
    }

    private fun assertInitialStateWithSingleScreen(transition: BackstackTransition) {
        val originalBackstack = listOf("one")
        compose.setContent {
            Backstack(originalBackstack, transition = transition) { Text(it) }
        }

        findByText("one").assertIsDisplayed()
    }

    private fun assertInitialStateWithMultipleScreens(transition: BackstackTransition) {
        val originalBackstack = listOf("one", "two")
        compose.setContent {
            Backstack(originalBackstack, transition = transition) { Text(it) }
        }

        findByText("two").assertIsDisplayed()

        findByText("one")
            .assertExists()
            // Check explicit semantics flag. We can't use assertIsNotDisplayed because it checks
            // layout bounds and the transition might hide the screen by just making it fully
            // transparent, but leaving it positioned on the screen.
            .assertIsHidden()
    }

    private fun assertTransition(transition: BackstackTransition) {
        val originalBackstack = listOf("one")
        val destinationBackstack = listOf("one", "two")
        val state = State(originalBackstack)
        compose.setContent {
            Providers(AnimationClockAmbient provides clock) {
                Backstack(
                    state.backstack,
                    animationBuilder = animation,
                    transition = transition
                ) { Text(it) }
            }
        }

        findByText("one").assertIsNotHidden()
        findByText("two").assertDoesNotExist()

        runOnUiThread {
            state.backstack = destinationBackstack
        }

        findByText("one").assertIsNotHidden()
        findByText("two").assertExists()
            .assertIsHidden()

        advanceTransition(.25f)

        findByText("one").assertIsNotHidden()
        findByText("two").assertIsNotHidden()

        advanceTransition(.75f)

        findByText("one").assertIsNotHidden()
        findByText("two").assertIsNotHidden()

        advanceTransition(1f)

        findByText("one").assertIsHidden()
        findByText("two").assertIsNotHidden()
    }

    private fun advanceTransition(percentage: Float) {
        runOnUiThread {
            clock.clockTimeMillis = (100 * percentage).toLong()
        }
    }
}
