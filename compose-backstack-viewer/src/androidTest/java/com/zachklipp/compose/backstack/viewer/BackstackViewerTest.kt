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

        findByText("Slide Transition").assertIsDisplayed()
        findBySubstring("Slow animations").assertIsDisplayed()

        findByText("one").assertIsSelected()
        findByText("one, two").assertIsUnselected()
        findByText("one, two, three").assertIsUnselected()

        findByText("Screen one").assertIsDisplayed()
        findBySubstring("Counter:").assertIsDisplayed()
    }

    @Test
    fun transitionBackFromSingleScreen() {
        compose.setContent {
            BackstackViewerApp()
        }

        findByTag(backTestTag("one")).assertHasClickAction().doClick()
        findByText("Screen one").assertIsDisplayed()
    }

    @Test
    fun transitionToSecondPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        findByText("Screen one").assertIsDisplayed()
        findByText("Screen two").assertDoesNotExist()

        findByText("one, two")
            .assertIsUnselected()
            .doClick()
            .assertIsSelected()

        findByText("Screen one").assertIsDisplayed()
        findByText("Screen two").assertIsDisplayed()
    }

    @Test
    fun transitionToThirdPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        findByText("Screen one").assertIsDisplayed()
        findByText("Screen two").assertDoesNotExist()
        findByText("Screen three").assertDoesNotExist()

        findByText("one, two, three")
            .assertIsUnselected()
            .doClick()
            .assertIsSelected()

        findByText("Screen one").assertIsDisplayed()
        findByText("Screen two").assertIsDisplayed()
        findByText("Screen three").assertIsDisplayed()
    }

    @Test
    fun transitionBackFromPrefabBackstack() {
        compose.setContent {
            BackstackViewerApp()
        }

        findByText("one, two, three").doClick().assertIsSelected()
        findByText("Screen three").assertIsDisplayed()

        findByTag(backTestTag("three")).doClick()
        findByText("one, two").assertIsSelected()
        findByText("Screen three").assertDoesNotExist()

        findByTag(backTestTag("two")).doClick()
        findByText("one").assertIsSelected()
        findByText("Screen two").assertDoesNotExist()
    }

    @Test
    fun addScreenWithFab() {
        compose.setContent {
            BackstackViewerApp()
        }

        findByTag(addTestTag("one")).assertHasClickAction().doClick()
        findByText("Screen one+").assertIsDisplayed()
        findByTag(backTestTag("one+")).assertHasClickAction().doClick()
        findByText("Screen one+").assertDoesNotExist()
    }
}
