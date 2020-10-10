package com.zachklipp.compose.backstack.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.android.createAndroidComposeRule
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.onNodeWithSubstring
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppTest {

  @get:Rule
  val compose = createAndroidComposeRule<ComposeBackstackActivity>()

  @Test
  fun launches() {
    onNodeWithSubstring("Slow animations")
  }

  @Test
  fun showsCounter() {
    onNodeWithSubstring("Counter:").assertIsDisplayed()
  }
}
