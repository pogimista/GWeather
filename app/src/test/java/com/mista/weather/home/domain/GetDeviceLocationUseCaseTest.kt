package com.mista.weather.home.domain

import com.mista.weather.base.BaseUseCase
import com.mista.weather.testutil.FakeLocationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GetDeviceLocationUseCaseTest {

    @Test
    fun `returns the provider's coordinates on success`() = runTest {
        val provider = FakeLocationProvider(coordinates = Coordinates(lat = 41.9, lon = 12.5))
        val useCase = GetDeviceLocationUseCase(provider)

        val result = useCase(BaseUseCase.NoParams)

        assertEquals(Coordinates(lat = 41.9, lon = 12.5), result.getOrNull())
    }

    @Test
    fun `returns null without failing when no location is available`() = runTest {
        val provider = FakeLocationProvider(coordinates = null)
        val useCase = GetDeviceLocationUseCase(provider)

        val result = useCase(BaseUseCase.NoParams)

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `wraps a provider exception into an AppError`() = runTest {
        val provider = FakeLocationProvider(throwable = IOException("gps unavailable"))
        val useCase = GetDeviceLocationUseCase(provider)

        val result = useCase(BaseUseCase.NoParams)

        assertTrue(result.isFailure)
    }
}
