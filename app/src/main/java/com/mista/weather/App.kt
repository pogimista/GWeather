package com.mista.weather

import android.app.Application
import com.mista.weather.di.networkModule
import com.mista.weather.di.sessionModule
import com.mista.weather.home.di.homeModule
import com.mista.weather.splash.di.splashModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(networkModule, sessionModule, splashModule, homeModule)
        }
    }
}
