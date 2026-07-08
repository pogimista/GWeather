package com.mista.weather.home.domain

import com.mista.weather.testutil.testWeather
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherTest {

    @Test
    fun `isPastSixPm is false before 6 PM at the location`() {
        val weather = testWeather(observedAt = 17 * 3600 + 59 * 60 + 59, timezoneOffsetSeconds = 0)
        assertFalse(weather.isPastSixPm)
    }

    @Test
    fun `isPastSixPm is true at exactly 6 PM at the location`() {
        val weather = testWeather(observedAt = 18 * 3600, timezoneOffsetSeconds = 0)
        assertTrue(weather.isPastSixPm)
    }

    @Test
    fun `isPastSixPm is true late at night at the location`() {
        val weather = testWeather(observedAt = 23 * 3600 + 30 * 60, timezoneOffsetSeconds = 0)
        assertTrue(weather.isPastSixPm)
    }

    @Test
    fun `isPastSixPm uses the location's timezone, not UTC`() {
        // 16:00 UTC, but the location is UTC+3 -> 19:00 local -> past 6 PM
        val weather = testWeather(observedAt = 16 * 3600L, timezoneOffsetSeconds = 3 * 3600L)
        assertTrue(weather.isPastSixPm)
    }

    @Test
    fun `isPastSixPm handles a negative timezone offset that wraps to the previous UTC day`() {
        // 02:00 UTC, location is UTC-5 -> 21:00 local the previous day -> past 6 PM
        val weather = testWeather(observedAt = 2 * 3600L, timezoneOffsetSeconds = -5 * 3600L)
        assertTrue(weather.isPastSixPm)
    }
}
