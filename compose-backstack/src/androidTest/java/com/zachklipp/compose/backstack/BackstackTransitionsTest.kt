package com.zachklipp.compose.backstack

import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zachklipp.compose.backstack.BackstackTransition.Crossfade
import com.zachklipp.compose.backstack.BackstackTransition.Slide
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackstackTransitionsTest {

  @get:Rule
  val compose = createComposeRule()

  private val animation = TweenSpec<Float>(durationMillis = 1000)

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
  fun transition_slide_forward() {
    assertTransition(Slide, forward = true)
  }

  @Test
  fun transition_crossfade_forward() {
    assertTransition(Crossfade, forward = true)
  }

  @Test
  fun transition_slide_backward() {
    assertTransition(Slide, forward = false)
  }

  @Test
  fun transition_crossfade_backward() {
    assertTransition(Crossfade, forward = false)
  }

  private fun assertInitialStateWithSingleScreen(transition: BackstackTransition) {
    val originalBackstack = listOf("one")
    compose.setContent {
      Backstack(originalBackstack, transition = transition) { BasicText(it) }
    }

    compose.onNodeWithText("one").assertIsDisplayed()
  }

  private fun assertInitialStateWithMultipleScreens(transition: BackstackTransition) {
    val originalBackstack = listOf("one", "two")
    compose.setContent {
      Backstack(originalBackstack, transition = transition) { BasicText(it) }
    }

    compose.onNodeWithText("two").assertIsDisplayed()
    compose.onNodeWithText("one").assertDoesNotExist()
  }

  private fun assertTransition(transition: BackstackTransition, forward: Boolean) {
    val firstBackstack = listOf("one")
    val secondBackstack = listOf("one", "two")
    var backstack by mutableStateOf(if (forward) firstBackstack else secondBackstack)
    compose.mainClock.autoAdvance = false
    compose.setContent {
      Backstack(
        backstack,
        frameController = rememberTransitionController(
          animationSpec = animation,
          transition = transition
        )
      ) { BasicText(it) }
    }
    val initialText = if (forward) "one" else "two"
    val newText = if (forward) "two" else "one"

    compose.onNodeWithText(initialText).assertIsDisplayed()
    compose.onNodeWithText(newText).assertDoesNotExist()

    compose.runOnUiThread {
      backstack = if (forward) secondBackstack else firstBackstack
    }

    compose.onNodeWithText(initialText).assertIsDisplayed()
    compose.onNodeWithText(newText).assertDoesNotExist()

    compose.mainClock.advanceTimeBy(250)

    compose.onNodeWithText(initialText).assertIsDisplayed()
    compose.onNodeWithText(newText).assertIsDisplayed()

    compose.mainClock.advanceTimeBy(750)

    compose.onNodeWithText(initialText).assertIsDisplayed()
    compose.onNodeWithText(newText).assertIsDisplayed()

    compose.mainClock.advanceTimeBy(1000)

    compose.onNodeWithText(initialText).assertDoesNotExist()
    compose.onNodeWithText(newText).assertIsDisplayed()
  }
}
