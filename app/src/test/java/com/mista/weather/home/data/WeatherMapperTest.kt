package com.mista.weather.home.data

import com.mista.weather.home.data.remote.dto.MainDto
import com.mista.weather.home.data.remote.dto.SysDto
import com.mista.weather.home.data.remote.dto.WeatherConditionDto
import com.mista.weather.home.data.remote.dto.WeatherResponseDto
import com.mista.weather.home.data.remote.dto.WindDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMapperTest {

    @Test
    fun `toDomain maps every field from a full response`() {
        val dto = WeatherResponseDto(
            name = "Reggio Emilia",
            weather = listOf(WeatherConditionDto(main = "Rain", description = "light rain", icon = "10d")),
            main = MainDto(temp = 21.5, feelsLike = 20.1, tempMin = 18.0, tempMax = 24.0, pressure = 1012, humidity = 60),
            wind = WindDto(speed = 4.2),
            sys = SysDto(country = "IT", sunrise = 1_700_000_000L, sunset = 1_700_040_000L),
            dt = 1_700_020_000L,
            timezone = 3600L,
        )

        val weather = dto.toDomain()

        assertEquals("Reggio Emilia", weather.cityName)
        assertEquals("IT", weather.country)
        assertEquals(21.5, weather.temperature, 0.0)
        assertEquals(20.1, weather.feelsLike, 0.0)
        assertEquals(18.0, weather.tempMin, 0.0)
        assertEquals(24.0, weather.tempMax, 0.0)
        assertEquals(60, weather.humidity)
        assertEquals(1012, weather.pressure)
        assertEquals(4.2, weather.windSpeed, 0.0)
        assertEquals("Rain", weather.condition)
        assertEquals("light rain", weather.description)
        assertEquals("10d", weather.iconCode)
        assertEquals(1_700_000_000L, weather.sunrise)
        assertEquals(1_700_040_000L, weather.sunset)
        assertEquals(1_700_020_000L, weather.observedAt)
        assertEquals(3600L, weather.timezoneOffsetSeconds)
    }

    @Test
    fun `toDomain falls back to safe defaults when optional fields are missing`() {
        val dto = WeatherResponseDto(
            name = "Unknown",
            weather = emptyList(),
            main = MainDto(temp = 10.0, feelsLike = 9.0, tempMin = 8.0, tempMax = 12.0, pressure = 1000, humidity = 40),
            wind = null,
            sys = null,
        )

        val weather = dto.toDomain()

        assertNull(weather.country)
        assertEquals(0.0, weather.windSpeed, 0.0)
        assertEquals("", weather.condition)
        assertEquals("", weather.description)
        assertEquals("", weather.iconCode)
        assertEquals(0L, weather.sunrise)
        assertEquals(0L, weather.sunset)
    }
}
