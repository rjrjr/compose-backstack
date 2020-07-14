package com.zachklipp.compose.backstack.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.android.AndroidComposeTestRule
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.findBySubstring
import androidx.ui.test.runOnIdleCompose
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppTest {

    @get:Rule
    val compose = AndroidComposeTestRule<ComposeBackstackActivity>()

    @Test
    fun launches() {
        // Without runOnIdleCompose, when this test runs first, the test fails with a message about
        // animations still running. However, adding the same wrapper to showsCounter causes that
        // test to hang. Is Compose leaking internal state between tests?
        runOnIdleCompose {
            findBySubstring("Slow animations")
        }
    }

    @Test
    fun showsCounter() {
        findBySubstring("Counter:").assertIsDisplayed()
    }
}
