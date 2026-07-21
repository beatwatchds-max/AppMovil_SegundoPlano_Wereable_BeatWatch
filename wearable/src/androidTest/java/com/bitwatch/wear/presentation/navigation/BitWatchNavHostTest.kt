package com.bitwatch.wear.presentation.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.bitwatch.wear.presentation.screens.ActivityScreen
import com.bitwatch.wear.presentation.screens.MainScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BitWatchNavHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun actividadButtonTriggersNavigationCallback() {
        var navigated = false

        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = {},
                    onNavigateToActivity = { navigated = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Actividad").performClick()
        composeTestRule.waitForIdle()
        assertTrue(navigated)
    }

    @Test
    fun mainScreenShowsBpmLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithText("bpm").assertIsDisplayed()
    }

    @Test
    fun activityScreenShowsTimerAndStartButton() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 0,
                    elapsedTimeMillis = 0L,
                    onStartClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("INICIAR").assertIsDisplayed()
    }

    @Test
    fun activityScreenShowsIntensityChips() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 0,
                    elapsedTimeMillis = 0L,
                    onStartClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Baja").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mod.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alta").assertIsDisplayed()
    }

    @Test
    fun stopButtonReturnsToStartText() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = {},
                    onStopClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("INICIAR").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("DETENER").assertIsDisplayed()

        composeTestRule.onNodeWithText("DETENER").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("INICIAR").assertIsDisplayed()
    }
}
