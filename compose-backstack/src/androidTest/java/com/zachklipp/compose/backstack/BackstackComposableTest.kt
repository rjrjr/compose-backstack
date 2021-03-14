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
class BackstackComposableTest {

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

  private fun assertTransition(transition: BackstackTransition) {
    val originalBackstack = listOf("one")
    val destinationBackstack = listOf("one", "two")
    var backstack by mutableStateOf(originalBackstack)
    compose.mainClock.autoAdvance = false
    compose.setContent {
        Backstack(
          backstack,
          animationBuilder = animation,
          transition = transition
        ) { BasicText(it) }
    }

    compose.onNodeWithText("one").assertIsDisplayed()
    compose.onNodeWithText("two").assertDoesNotExist()

    compose.runOnUiThread {
      backstack = destinationBackstack
    }

    compose.onNodeWithText("one").assertIsDisplayed()
    compose.onNodeWithText("two").assertDoesNotExist()

    compose.mainClock.advanceTimeBy(250)

    compose.onNodeWithText("one").assertIsDisplayed()
    compose.onNodeWithText("two").assertIsDisplayed()

    compose.mainClock.advanceTimeBy(750)

    compose.onNodeWithText("one").assertIsDisplayed()
    compose.onNodeWithText("two").assertIsDisplayed()

    compose.mainClock.advanceTimeBy(1000)

    compose.onNodeWithText("one").assertDoesNotExist()
    compose.onNodeWithText("two").assertIsDisplayed()
  }
}
