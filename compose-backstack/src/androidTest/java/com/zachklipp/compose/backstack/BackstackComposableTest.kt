package com.zachklipp.compose.backstack

import androidx.animation.ManualAnimationClock
import androidx.animation.TweenBuilder
import androidx.compose.Model
import androidx.compose.Providers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.foundation.Text
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.findByText
import androidx.ui.test.runOnUiThread
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
        findByText("one").assertDoesNotExist()
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

        findByText("one").assertIsDisplayed()
        findByText("two").assertDoesNotExist()

        runOnUiThread {
            state.backstack = destinationBackstack
        }

        findByText("one").assertIsDisplayed()
        findByText("two").assertDoesNotExist()

        setTransitionTime(25)

        findByText("one").assertIsDisplayed()
        findByText("two").assertIsDisplayed()

        setTransitionTime(75)

        findByText("one").assertIsDisplayed()
        findByText("two").assertIsDisplayed()

        setTransitionTime(100)

        findByText("one").assertDoesNotExist()
        findByText("two").assertIsDisplayed()
    }

    private fun setTransitionTime(time: Long) {
        runOnUiThread {
            clock.clockTimeMillis = time
        }
    }
}
