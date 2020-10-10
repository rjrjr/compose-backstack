package com.zachklipp.compose.backstack

import androidx.compose.animation.core.ManualAnimationClock
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Text
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.onNodeWithText
import androidx.ui.test.runOnUiThread
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackstackComposableTest {

  @get:Rule
  val compose = createComposeRule()

  private val clock = ManualAnimationClock(0)
  private val animation = TweenSpec<Float>(durationMillis = 100)

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

    onNodeWithText("one").assertIsDisplayed()
  }

  private fun assertInitialStateWithMultipleScreens(transition: BackstackTransition) {
    val originalBackstack = listOf("one", "two")
    compose.setContent {
      Backstack(originalBackstack, transition = transition) { Text(it) }
    }

    onNodeWithText("two").assertIsDisplayed()
    onNodeWithText("one").assertDoesNotExist()
  }

  private fun assertTransition(transition: BackstackTransition) {
    val originalBackstack = listOf("one")
    val destinationBackstack = listOf("one", "two")
    var backstack by mutableStateOf(originalBackstack)
    compose.setContent {
      Providers(AnimationClockAmbient provides clock) {
        Backstack(
            backstack,
            animationBuilder = animation,
            transition = transition
        ) { Text(it) }
      }
    }

    onNodeWithText("one").assertIsDisplayed()
    onNodeWithText("two").assertDoesNotExist()

    runOnUiThread {
      backstack = destinationBackstack
    }

    onNodeWithText("one").assertIsDisplayed()
    onNodeWithText("two").assertDoesNotExist()

    setTransitionTime(25)

    onNodeWithText("one").assertIsDisplayed()
    onNodeWithText("two").assertIsDisplayed()

    setTransitionTime(75)

    onNodeWithText("one").assertIsDisplayed()
    onNodeWithText("two").assertIsDisplayed()

    setTransitionTime(100)

    onNodeWithText("one").assertDoesNotExist()
    onNodeWithText("two").assertIsDisplayed()
  }

  private fun setTransitionTime(time: Long) {
    runOnUiThread {
      clock.clockTimeMillis = time
    }
  }
}
