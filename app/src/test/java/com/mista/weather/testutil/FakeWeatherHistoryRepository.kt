package com.mista.weather.testutil

import com.mista.weather.home.data.WeatherHistoryRepository
import com.mista.weather.home.domain.Weather
import com.mista.weather.home.domain.WeatherHistoryEntry

class FakeWeatherHistoryRepository : WeatherHistoryRepository {

    private val entries = mutableListOf<WeatherHistoryEntry>()

    override fun getHistory(): List<WeatherHistoryEntry> = entries.toList()

    override fun addEntry(weather: Weather) {
        entries.add(0, WeatherHistoryEntry(id = entries.size.toLong(), fetchedAt = 0L, weather = weather))
    }
}
