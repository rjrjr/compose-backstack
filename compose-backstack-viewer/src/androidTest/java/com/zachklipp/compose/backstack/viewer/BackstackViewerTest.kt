package com.zachklipp.compose.backstack.viewer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.ui.test.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackstackViewerTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun initialState() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithText("Slide Transition").assertIsDisplayed()
        onNodeWithSubstring("Slow animations").assertIsDisplayed()

        onNodeWithText("one").assertIsSelected()
        onNodeWithText("one, two").assertIsNotSelected()
        onNodeWithText("one, two, three").assertIsNotSelected()

        onNodeWithText("Screen one").assertIsDisplayed()
        onNodeWithSubstring("Counter:").assertIsDisplayed()
    }

    @Test
    fun transitionBackFromSingleScreen() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithTag(backTestTag("one")).assertHasClickAction().performClick()
        onNodeWithText("Screen one").assertIsDisplayed()
    }

    @Test
    fun transitionToSecondPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithText("Screen one").assertIsDisplayed()
        onNodeWithText("Screen two").assertDoesNotExist()

        onNodeWithText("one, two")
            .assertIsNotSelected()
            .performClick()
            .assertIsSelected()

        onNodeWithText("Screen one").assertDoesNotExist()
        onNodeWithText("Screen two").assertIsDisplayed()
    }

    @Test
    fun transitionToThirdPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithText("Screen one").assertIsDisplayed()
        onNodeWithText("Screen two").assertDoesNotExist()
        onNodeWithText("Screen three").assertDoesNotExist()

        onNodeWithText("one, two, three")
            .assertIsNotSelected()
            .performClick()
            .assertIsSelected()

        onNodeWithText("Screen one").assertDoesNotExist()
        onNodeWithText("Screen two").assertDoesNotExist()
        onNodeWithText("Screen three").assertIsDisplayed()
    }

    @Test
    fun transitionBackFromPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithText("one, two, three").performClick().assertIsSelected()
        onNodeWithText("Screen three").assertIsDisplayed()

        onNodeWithTag(backTestTag("three")).performClick()
        onNodeWithText("one, two").assertIsSelected()
        onNodeWithText("Screen three").assertDoesNotExist()

        onNodeWithTag(backTestTag("two")).performClick()
        onNodeWithText("one").assertIsSelected()
        onNodeWithText("Screen two").assertDoesNotExist()
    }

    @Test
    fun addScreenWithFab() {
        compose.setContent {
            BackstackViewerApp()
        }

        onNodeWithTag(addTestTag("one")).assertHasClickAction().performClick()
        onNodeWithText("Screen one+").assertIsDisplayed()
        onNodeWithTag(backTestTag("one+")).assertHasClickAction().performClick()
        onNodeWithText("Screen one+").assertDoesNotExist()
    }
}
