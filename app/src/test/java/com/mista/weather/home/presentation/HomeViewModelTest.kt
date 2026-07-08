package com.mista.weather.home.presentation

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.mista.weather.R
import com.mista.weather.base.BaseViewModelDeps
import com.mista.weather.base.ScreenResultBus
import com.mista.weather.home.domain.Coordinates
import com.mista.weather.home.domain.GetCurrentWeatherUseCase
import com.mista.weather.home.domain.GetDeviceLocationUseCase
import com.mista.weather.home.domain.Weather
import com.mista.weather.session.CacheSession
import com.mista.weather.session.TransientSession
import com.mista.weather.testutil.FakeLocationProvider
import com.mista.weather.testutil.FakeWeatherHistoryRepository
import com.mista.weather.testutil.FakeWeatherRepository
import com.mista.weather.testutil.testWeather
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(ContextCompat::class)
        application = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
        Dispatchers.resetMain()
    }

    private fun stubPermission(granted: Boolean) {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns
            if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
    }

    private fun buildViewModel(
        weatherResult: Result<Weather> = Result.success(testWeather()),
        deviceCoordinates: Coordinates? = null,
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository(weatherResult),
        locationProvider: FakeLocationProvider = FakeLocationProvider(deviceCoordinates),
        historyRepository: FakeWeatherHistoryRepository = FakeWeatherHistoryRepository(),
    ): HomeViewModel {
        val deps = BaseViewModelDeps(
            application = application,
            cacheSession = mockk<CacheSession>(relaxed = true),
            transientSession = TransientSession(),
            moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
            screenResultBus = ScreenResultBus(),
        )
        return HomeViewModel(
            deps = deps,
            getCurrentWeatherUseCase = GetCurrentWeatherUseCase(weatherRepository),
            getDeviceLocationUseCase = GetDeviceLocationUseCase(locationProvider),
            weatherHistoryRepository = historyRepository,
        )
    }

    @Test
    fun `uses the default coordinates when location permission is not granted`() = runTest(testDispatcher) {
        stubPermission(granted = false)
        val weatherRepository = FakeWeatherRepository(Result.success(testWeather()))

        buildViewModel(weatherRepository = weatherRepository)
        advanceUntilIdle()

        assertEquals(44.34, weatherRepository.lastLat!!, 0.0)
        assertEquals(10.99, weatherRepository.lastLon!!, 0.0)
    }

    @Test
    fun `uses the device location when permission is granted and a fix is available`() = runTest(testDispatcher) {
        stubPermission(granted = true)
        val weatherRepository = FakeWeatherRepository(Result.success(testWeather()))

        buildViewModel(
            weatherRepository = weatherRepository,
            deviceCoordinates = Coordinates(lat = 48.85, lon = 2.35),
        )
        advanceUntilIdle()

        assertEquals(48.85, weatherRepository.lastLat!!, 0.0)
        assertEquals(2.35, weatherRepository.lastLon!!, 0.0)
    }

    @Test
    fun `falls back to default coordinates when permission is granted but no fix is available`() = runTest(testDispatcher) {
        stubPermission(granted = true)
        val weatherRepository = FakeWeatherRepository(Result.success(testWeather()))

        buildViewModel(weatherRepository = weatherRepository, deviceCoordinates = null)
        advanceUntilIdle()

        assertEquals(44.34, weatherRepository.lastLat!!, 0.0)
        assertEquals(10.99, weatherRepository.lastLon!!, 0.0)
    }

    @Test
    fun `a successful fetch publishes Success and records a history entry`() = runTest(testDispatcher) {
        stubPermission(granted = false)
        val weather = testWeather(cityName = "Bologna")
        val historyRepository = FakeWeatherHistoryRepository()

        val viewModel = buildViewModel(weatherResult = Result.success(weather), historyRepository = historyRepository)
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(weather, (state as HomeUiState.Success).weather)
        assertEquals(1, viewModel.historyState.value.size)
        assertEquals(1, historyRepository.getHistory().size)
    }

    @Test
    fun `a failed fetch publishes an Error state with a mapped message`() = runTest(testDispatcher) {
        stubPermission(granted = false)
        every { application.getString(R.string.error_network) } returns "No internet connection"

        val viewModel = buildViewModel(weatherResult = Result.failure(IOException("boom")))
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals("No internet connection", (state as HomeUiState.Error).message)
    }

    @Test
    fun `retry re-fetches the weather`() = runTest(testDispatcher) {
        stubPermission(granted = false)
        val weatherRepository = FakeWeatherRepository(Result.success(testWeather()))

        val viewModel = buildViewModel(weatherRepository = weatherRepository)
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, weatherRepository.callCount)
    }

    @Test
    fun `granting location permission after denial switches to the device location on the next load`() = runTest(testDispatcher) {
        stubPermission(granted = false)
        val weatherRepository = FakeWeatherRepository(Result.success(testWeather()))
        val locationProvider = FakeLocationProvider(coordinates = Coordinates(lat = 51.5, lon = -0.12))

        val viewModel = buildViewModel(weatherRepository = weatherRepository, locationProvider = locationProvider)
        advanceUntilIdle()
        assertEquals(44.34, weatherRepository.lastLat!!, 0.0)

        stubPermission(granted = true)
        viewModel.onLocationPermissionResult(true)
        advanceUntilIdle()

        assertEquals(51.5, weatherRepository.lastLat!!, 0.0)
        assertEquals(-0.12, weatherRepository.lastLon!!, 0.0)
        assertTrue(viewModel.locationPermissionGranted.value)
    }
}
