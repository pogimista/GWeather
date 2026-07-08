package com.mista.weather.testutil

import com.mista.weather.home.data.WeatherRepository
import com.mista.weather.home.domain.Weather

class FakeWeatherRepository(private val result: Result<Weather>) : WeatherRepository {

    var lastLat: Double? = null
        private set
    var lastLon: Double? = null
        private set
    var callCount: Int = 0
        private set

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Weather {
        lastLat = lat
        lastLon = lon
        callCount++
        return result.getOrThrow()
    }
}
