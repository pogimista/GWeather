package com.mista.weather.home.di

import com.mista.weather.home.data.WeatherRepository
import com.mista.weather.home.data.WeatherRepositoryImpl
import com.mista.weather.home.data.remote.WeatherApiService
import com.mista.weather.home.domain.GetCurrentWeatherUseCase
import com.mista.weather.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit

val homeModule = module {
    single { get<Retrofit>().create(WeatherApiService::class.java) }
    single<WeatherRepository> { WeatherRepositoryImpl(get()) }
    factory { GetCurrentWeatherUseCase(get()) }
    viewModelOf(::HomeViewModel)
}
