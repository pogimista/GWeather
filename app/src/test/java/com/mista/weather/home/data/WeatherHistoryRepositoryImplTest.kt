package com.mista.weather.home.data

import com.mista.weather.testutil.FakeSharedPreferences
import com.mista.weather.testutil.testWeather
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherHistoryRepositoryImplTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private lateinit var repository: WeatherHistoryRepositoryImpl

    @Before
    fun setUp() {
        repository = WeatherHistoryRepositoryImpl(FakeSharedPreferences(), moshi)
    }

    @Test
    fun `getHistory is empty before anything has been recorded`() {
        assertTrue(repository.getHistory().isEmpty())
    }

    @Test
    fun `addEntry persists the entry and survives a round trip through prefs`() {
        repository.addEntry(testWeather(cityName = "Turin"))

        val history = repository.getHistory()

        assertEquals(1, history.size)
        assertEquals("Turin", history.first().weather.cityName)
    }

    @Test
    fun `newest entry is returned first`() {
        repository.addEntry(testWeather(cityName = "First"))
        repository.addEntry(testWeather(cityName = "Second"))

        val history = repository.getHistory()

        assertEquals("Second", history[0].weather.cityName)
        assertEquals("First", history[1].weather.cityName)
    }

    @Test
    fun `history is capped at 30 entries, dropping the oldest`() {
        repeat(35) { index -> repository.addEntry(testWeather(cityName = "City $index")) }

        val history = repository.getHistory()

        assertEquals(30, history.size)
        assertEquals("City 34", history.first().weather.cityName)
        assertEquals("City 5", history.last().weather.cityName)
    }
}
