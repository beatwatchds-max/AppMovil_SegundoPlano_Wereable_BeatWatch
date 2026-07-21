package com.bitwatch.wear.presentation.viewmodel

import com.bitwatch.wear.data.HeartRateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `initial state has default values`() {
        val repo = mockk<HeartRateRepository>(relaxed = true)
        val viewModel = MainViewModel(repo)

        val state = viewModel.uiState.value
        assertEquals(0, state.bpm)
        assertTrue(state.isSensorAvailable)
        assertFalse(state.isReading)
    }

    @Test
    fun `startSampling sets isSensorAvailable false when sensor unavailable`() = runTest {
        val repo = mockk<HeartRateRepository>()
        coEvery { repo.hasHeartRateCapability() } returns false

        val viewModel = MainViewModel(repo)
        viewModel.startSampling()
        advanceTimeBy(1)

        assertFalse(viewModel.uiState.value.isSensorAvailable)
        assertEquals(0, viewModel.uiState.value.bpm)
        coVerify { repo.hasHeartRateCapability() }
    }

    @Test
    fun `startSampling updates bpm when reading succeeds`() = runTest {
        val repo = mockk<HeartRateRepository>()
        coEvery { repo.hasHeartRateCapability() } returns true
        coEvery { repo.takeSingleReading(any()) } returns 75

        val viewModel = MainViewModel(repo)
        viewModel.samplingIntervalMs = 1L
        viewModel.startSampling()
        advanceTimeBy(1)

        val state = viewModel.uiState.value
        assertEquals(75, state.bpm)
        assertTrue(state.isSensorAvailable)

        viewModel.stopSampling()
    }

    @Test
    fun `startSampling keeps bpm at zero when reading returns null`() = runTest {
        val repo = mockk<HeartRateRepository>()
        coEvery { repo.hasHeartRateCapability() } returns true
        coEvery { repo.takeSingleReading(any()) } returns null

        val viewModel = MainViewModel(repo)
        viewModel.samplingIntervalMs = 1L
        viewModel.startSampling()
        advanceTimeBy(1)

        assertEquals(0, viewModel.uiState.value.bpm)
        assertFalse(viewModel.uiState.value.isReading)

        viewModel.stopSampling()
    }

    @Test
    fun `stopSampling cancels the sampling job`() = runTest {
        val repo = mockk<HeartRateRepository>()
        coEvery { repo.hasHeartRateCapability() } returns true
        coEvery { repo.takeSingleReading(any()) } returns 80

        val viewModel = MainViewModel(repo)
        viewModel.samplingIntervalMs = 1L
        viewModel.startSampling()
        advanceTimeBy(1)
        assertEquals(80, viewModel.uiState.value.bpm)

        viewModel.stopSampling()
        assertFalse(viewModel.uiState.value.isReading)
    }

    @Test
    fun `startSampling called twice does not start duplicate jobs`() = runTest {
        val repo = mockk<HeartRateRepository>()
        coEvery { repo.hasHeartRateCapability() } returns true
        coEvery { repo.takeSingleReading(any()) } returns 60

        val viewModel = MainViewModel(repo)
        viewModel.samplingIntervalMs = 1L

        viewModel.startSampling()
        advanceTimeBy(1)
        assertEquals(60, viewModel.uiState.value.bpm)

        viewModel.startSampling()
        advanceTimeBy(1)

        assertEquals(60, viewModel.uiState.value.bpm)

        viewModel.stopSampling()
    }
}
