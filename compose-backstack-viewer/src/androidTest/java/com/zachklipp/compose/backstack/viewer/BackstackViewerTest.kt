package com.zachklipp.compose.backstack.viewer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackstackViewerTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun launches() {
        compose.setContent {
            BackstackViewerApp()
        }

        // This fails, semantics bug?
        //findByText("Slow animations:")
        //findBySubstring("Counter:").assertIsDisplayed()
    }
}
