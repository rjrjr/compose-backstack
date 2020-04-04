package com.zachklipp.compose.backstack.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.android.AndroidComposeTestRule
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.findBySubstring
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppTest {

    @get:Rule
    val compose = AndroidComposeTestRule<ComposeBackstackActivity>()

    @Test
    fun launches() {
        // This fails, semantics bug?
        findBySubstring("Slow animations")
    }

    @Test
    fun showsCounter() {
        findBySubstring("Counter:").assertIsDisplayed()
    }
}
