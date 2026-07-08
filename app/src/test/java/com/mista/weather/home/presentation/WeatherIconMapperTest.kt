package com.mista.weather.home.presentation

import com.mista.weather.testutil.testWeather
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherIconMapperTest {

    @Test
    fun `clear condition shows a sun icon before 6 PM`() {
        assertEquals("☀️", weatherEmoji(condition = "Clear", isPastSixPm = false))
    }

    @Test
    fun `clear condition shows a moon icon past 6 PM`() {
        assertEquals("🌙", weatherEmoji(condition = "Clear", isPastSixPm = true))
    }

    @Test
    fun `rain condition always shows a rain icon, day or night`() {
        assertEquals("🌧️", weatherEmoji(condition = "Rain", isPastSixPm = false))
        assertEquals("🌧️", weatherEmoji(condition = "Rain", isPastSixPm = true))
    }

    @Test
    fun `drizzle is treated as rain`() {
        assertEquals("🌧️", weatherEmoji(condition = "Drizzle", isPastSixPm = false))
    }

    @Test
    fun `condition matching is case insensitive`() {
        assertEquals("☀️", weatherEmoji(condition = "cLeAr", isPastSixPm = false))
    }

    @Test
    fun `unknown condition falls back to a generic icon`() {
        assertEquals("🌡️", weatherEmoji(condition = "Volcanic", isPastSixPm = false))
    }

    @Test
    fun `weatherEmoji overload derives past-6pm from the weather's own location time`() {
        val dayWeather = testWeather(condition = "Clear", observedAt = 12 * 3600L, timezoneOffsetSeconds = 0L)
        val nightWeather = testWeather(condition = "Clear", observedAt = 22 * 3600L, timezoneOffsetSeconds = 0L)

        assertEquals("☀️", weatherEmoji(dayWeather))
        assertEquals("🌙", weatherEmoji(nightWeather))
    }

    @Test
    fun `formatLocalTime renders HH-mm at the location's offset, not UTC`() {
        // 10:00 UTC, location is UTC+2 -> 12:00 local
        val result = formatLocalTime(epochSeconds = 10 * 3600L, timezoneOffsetSeconds = 2 * 3600L)
        assertEquals("12:00", result)
    }

    @Test
    fun `formatLocalTime wraps correctly across a day boundary`() {
        // 23:00 UTC, location is UTC+3 -> 02:00 local the next day
        val result = formatLocalTime(epochSeconds = 23 * 3600L, timezoneOffsetSeconds = 3 * 3600L)
        assertEquals("02:00", result)
    }
}
