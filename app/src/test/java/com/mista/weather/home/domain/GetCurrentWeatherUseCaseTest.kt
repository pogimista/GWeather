package com.mista.weather.home.domain

import com.mista.weather.base.error.AppError
import com.mista.weather.testutil.FakeWeatherRepository
import com.mista.weather.testutil.testWeather
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GetCurrentWeatherUseCaseTest {

    @Test
    fun `returns the mapped weather on success and forwards coordinates to the repository`() = runTest {
        val weather = testWeather(cityName = "Milan")
        val repository = FakeWeatherRepository(Result.success(weather))
        val useCase = GetCurrentWeatherUseCase(repository)

        val result = useCase(Coordinates(lat = 45.46, lon = 9.19))

        assertEquals(weather, result.getOrNull())
        assertEquals(45.46, repository.lastLat!!, 0.0)
        assertEquals(9.19, repository.lastLon!!, 0.0)
    }

    @Test
    fun `wraps a repository failure into an AppError instead of throwing`() = runTest {
        val repository = FakeWeatherRepository(Result.failure(IOException("no network")))
        val useCase = GetCurrentWeatherUseCase(repository)

        val result = useCase(Coordinates(lat = 0.0, lon = 0.0))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.Network)
    }
}
