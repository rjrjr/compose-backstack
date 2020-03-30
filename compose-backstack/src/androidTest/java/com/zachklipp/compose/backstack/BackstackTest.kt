package com.zachklipp.compose.backstack

import androidx.animation.ManualAnimationClock
import androidx.animation.TweenBuilder
import androidx.compose.Model
import androidx.compose.Providers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.core.Text
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.findByText
import com.google.common.truth.ExpectFailure.expectFailure
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Model
private class State(
    var backstack: List<String>
)

@RunWith(AndroidJUnit4::class)
class BackstackTest {

    @get:Rule
    val compose = createComposeRule()

    private val clock = ManualAnimationClock(0)
    private val animation = TweenBuilder<Float>().apply { duration = 100 }

    @Test
    fun initialStateWithSingleScreen() {
        val originalBackstack = listOf("one")
        compose.setContent {
            Backstack(originalBackstack) { Text(it) }
        }

        findByText("one").assertIsDisplayed()
    }

    @Ignore("isDisplayed check doesn't work if backstack has more than 1 item")
    @Test
    fun initialStateWithMultipleScreens() {
        val originalBackstack = listOf("one", "two")
        compose.setContent {
            Backstack(originalBackstack) { Text(it) }
        }

        findByText("two").assertIsDisplayed()

        expectFailure {
            findByText("one")
        }
    }

    @Ignore("isDisplayed check doesn't work if backstack has more than 1 item")
    @Test
    fun transition() {
        val originalBackstack = listOf("one")
        val destinationBackstack = listOf("one", "two")
        val state = State(originalBackstack)
        compose.setContent {
            Providers(AnimationClockAmbient provides clock) {
                Backstack(state.backstack, animationBuilder = animation) { Text(it) }
            }
        }

        findByText("one").assertIsDisplayed()
        findByText("two").assertDoesNotExist()

        compose.runOnUiThread {
            state.backstack = destinationBackstack
        }

        findByText("one").assertIsDisplayed()
        findByText("two").assertDoesNotExist()

        advanceTransition(.25f)

        findByText("one").assertIsDisplayed()
        findByText("two").assertIsDisplayed()

        advanceTransition(.75f)

        findByText("one").assertIsDisplayed()
        findByText("two").assertIsDisplayed()

        advanceTransition(1f)
    }

    private fun advanceTransition(percentage: Float) {
        compose.runOnUiThread {
            clock.clockTimeMillis = (100 * percentage).toLong()
        }
    }
}
