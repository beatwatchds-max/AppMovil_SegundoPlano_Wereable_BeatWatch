package com.bitwatch.wear.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.wear.compose.material.MaterialTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActivityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTimerInitially() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun displaysBpmText() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 85,
                    elapsedTimeMillis = 0L,
                    onStartClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("85 bpm").assertIsDisplayed()
    }

    @Test
    fun displaysStartButtonInitially() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("INICIAR").assertIsDisplayed()
    }

    @Test
    fun displaysAllIntensityChips() {
        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
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
    fun startClickTriggersCallback() {
        var capturedIntensity: Intensity? = null

        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = { intensity -> capturedIntensity = intensity }
                )
            }
        }

        composeTestRule.onNodeWithTag("StartStopButton").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Intensity.MODERATE, capturedIntensity)
    }

    @Test
    fun intensityChipSelection() {
        var selectedIntensity: Intensity? = null

        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = { intensity -> selectedIntensity = intensity }
                )
            }
        }

        composeTestRule.onNodeWithTag("IntensityChip_Alta").performClick()
        composeTestRule.onNodeWithTag("StartStopButton").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Intensity.HIGH, selectedIntensity)
    }

    @Test
    fun stopClickTriggersCallback() {
        var stopped = false

        composeTestRule.setContent {
            MaterialTheme {
                ActivityScreen(
                    bpm = 80,
                    elapsedTimeMillis = 0L,
                    onStartClick = {},
                    onStopClick = { stopped = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("StartStopButton").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("DETENER").assertIsDisplayed()

        composeTestRule.onNodeWithTag("StartStopButton").performClick()
        composeTestRule.waitForIdle()
        assertEquals(true, stopped)
        composeTestRule.onNodeWithText("INICIAR").assertIsDisplayed()
    }
}