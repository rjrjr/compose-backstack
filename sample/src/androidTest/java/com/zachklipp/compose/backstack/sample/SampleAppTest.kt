package com.zachklipp.compose.backstack.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
    compose.onNodeWithText("Slow animations", substring = true)
  }

  @Test
  fun showsCounter() {
    compose.onNodeWithText("Counter:", substring = true).assertIsDisplayed()
  }
}
