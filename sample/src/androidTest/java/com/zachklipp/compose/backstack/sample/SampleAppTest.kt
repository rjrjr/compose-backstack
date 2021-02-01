package com.zachklipp.compose.backstack.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppTest {

  @get:Rule
  val compose = createAndroidComposeRule<ComposeBackstackActivity>()

  @Test
  fun launches() {
    compose.onNodeWithSubstring("Slow animations")
  }

  @Test
  fun showsCounter() {
    compose.onNodeWithSubstring("Counter:").assertIsDisplayed()
  }
}
