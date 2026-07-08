package com.mista.weather.testutil

import com.mista.weather.home.data.location.LocationProvider
import com.mista.weather.home.domain.Coordinates

class FakeLocationProvider(
    private val coordinates: Coordinates? = null,
    private val throwable: Throwable? = null,
) : LocationProvider {

    override suspend fun getCurrentLocation(): Coordinates? {
        throwable?.let { throw it }
        return coordinates
    }
}
