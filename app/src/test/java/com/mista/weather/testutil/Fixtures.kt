package com.mista.weather.testutil

import com.mista.weather.home.domain.Weather

fun testWeather(
    cityName: String = "Reggio Emilia",
    country: String? = "IT",
    temperature: Double = 21.0,
    feelsLike: Double = 20.0,
    tempMin: Double = 18.0,
    tempMax: Double = 24.0,
    humidity: Int = 55,
    pressure: Int = 1013,
    windSpeed: Double = 3.5,
    condition: String = "Clear",
    description: String = "clear sky",
    iconCode: String = "01d",
    sunrise: Long = 0L,
    sunset: Long = 0L,
    observedAt: Long = 0L,
    timezoneOffsetSeconds: Long = 0L,
): Weather = Weather(
    cityName = cityName,
    country = country,
    temperature = temperature,
    feelsLike = feelsLike,
    tempMin = tempMin,
    tempMax = tempMax,
    humidity = humidity,
    pressure = pressure,
    windSpeed = windSpeed,
    condition = condition,
    description = description,
    iconCode = iconCode,
    sunrise = sunrise,
    sunset = sunset,
    observedAt = observedAt,
    timezoneOffsetSeconds = timezoneOffsetSeconds,
)
