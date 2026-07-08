package com.mista.weather.di

import com.mista.weather.BuildConfig
import com.mista.weather.home.data.remote.WeatherApiKeyInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(WeatherApiKeyInterceptor(BuildConfig.OPEN_WEATHER_API_KEY))
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .build()
    }

    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }
}
