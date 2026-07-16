package com.bitwatch.wear.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.wear.compose.material.MaterialTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysBpmValue() {
        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 75,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithText("75").assertIsDisplayed()
    }

    @Test
    fun displaysBpmLabel() {
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
    fun displaysElapsedTimeLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hace", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysAllModeButtons() {
        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Reposo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Actividad").assertIsDisplayed()
        composeTestRule.onNodeWithText("Normal").assertIsDisplayed()
    }

    @Test
    fun modeSelectionTriggersCallback() {
        var selectedMode: Mode? = null

        composeTestRule.setContent {
            MaterialTheme {
                MainScreen(
                    bpm = 72,
                    lastReadingTime = System.currentTimeMillis(),
                    onModeSelected = { mode -> selectedMode = mode }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Actividad").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Mode.ACTIVITY, selectedMode)

        composeTestRule.onNodeWithContentDescription("Reposo").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Mode.REST, selectedMode)

        composeTestRule.onNodeWithContentDescription("Normal").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Mode.NORMAL, selectedMode)
    }
}