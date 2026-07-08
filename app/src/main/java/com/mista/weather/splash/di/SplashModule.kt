package com.mista.weather.splash.di

import com.mista.weather.splash.presentation.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val splashModule = module {
    viewModelOf(::SplashViewModel)
}
