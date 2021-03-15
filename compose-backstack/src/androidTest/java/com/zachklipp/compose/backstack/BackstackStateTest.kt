package com.zachklipp.compose.backstack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class BackstackStateTest {

  @get:Rule
  val compose = createComposeRule()

  @Test fun screen_state_is_restored_on_pop() {
    val backstack = mutableStateListOf("one")
    compose.setContent {
      Backstack(backstack, frameController = NoopFrameController()) {
        var counter by rememberSaveable { mutableStateOf(0) }
        BasicText("$it: $counter", Modifier.clickable { counter++ })
      }
    }

    // Update some state on the first screen.
    compose.onNodeWithText("one: 0").assertIsDisplayed().performClick()
    compose.onNodeWithText("one: 1").assertIsDisplayed()

    // Navigate forward to another screen.
    backstack += "two"
    compose.waitForIdle()

    compose.onNodeWithText("one", substring = true).assertDoesNotExist()
    compose.onNodeWithText("two: 0").assertIsDisplayed()

    // Navigate back.
    backstack -= "two"
    compose.waitForIdle()

    // Make sure the state was restored.
    compose.onNodeWithText("one: 1").assertIsDisplayed()
  }

  @Test fun screen_state_is_discarded_after_pop() {
    val backstack = mutableStateListOf("one", "two")
    compose.setContent {
      Backstack(backstack, frameController = NoopFrameController()) {
        var counter by rememberSaveable { mutableStateOf(0) }
        BasicText("$it: $counter", Modifier.clickable { counter++ })
      }
    }

    // Update some state on the second screen.
    compose.onNodeWithText("two: 0").assertIsDisplayed().performClick()
    compose.onNodeWithText("two: 1").assertIsDisplayed()

    // Navigate backwards then forwards again to the same second screen.
    backstack -= "two"
    compose.waitForIdle()
    backstack += "two"
    compose.waitForIdle()

    compose.onNodeWithText("two: 0").assertIsDisplayed()
    compose.onNodeWithText("two: 1").assertDoesNotExist()
  }

  @Test fun screen_state_is_discarded_when_removed_from_backstack_while_hidden() {
    var backstack by mutableStateOf(listOf("one"))
    compose.setContent {
      Backstack(backstack, frameController = NoopFrameController()) {
        var counter by rememberSaveable { mutableStateOf(0) }
        BasicText("$it: $counter", Modifier.clickable { counter++ })
      }
    }

    // Update some state on the first screen.
    compose.onNodeWithText("one: 0").assertIsDisplayed().performClick()
    compose.onNodeWithText("one: 1").assertIsDisplayed()

    // Navigate forward to another screen.
    backstack = listOf("one", "two")
    compose.waitForIdle()

    // Remove one from the backstack - this should clear its saved state, even though it's currently
    // hidden.
    backstack = listOf("two")
    compose.waitForIdle()

    // Add it back so we can navigate back, then do so.
    backstack = listOf("one", "two")
    compose.waitForIdle()
    backstack = listOf("one")
    compose.waitForIdle()

    // Make sure the state was restored.
    compose.onNodeWithText("one: 0").assertIsDisplayed()
    compose.onNodeWithText("one: 1").assertDoesNotExist()
  }

  @Test fun screens_are_skipped() {
    val backstack = mutableStateListOf("one")
    val transcript = mutableListOf<String>()
    compose.setContent {
      Backstack(backstack, frameController = NoopFrameController()) {
        BasicText(it)
        DisposableEffect(Unit) {
          transcript += "+$it"
          onDispose { transcript += "-$it" }
        }
      }
    }

    assertThat(transcript).containsExactly("+one")
    transcript.clear()

    // Navigate forward.
    backstack += "two"
    compose.waitForIdle()

    assertThat(transcript).containsExactly("-one", "+two")
    transcript.clear()

    // Navigate back again.
    backstack -= "two"
    compose.waitForIdle()

    assertThat(transcript).containsExactly("-two", "+one")
  }
}
