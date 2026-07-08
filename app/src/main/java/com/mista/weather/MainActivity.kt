package com.mista.weather

import android.os.Bundle
import com.mista.weather.base.BaseActivity
import com.mista.weather.base.BaseKey
import com.mista.weather.splash.presentation.SplashKey

class MainActivity : BaseActivity() {

    override val initialKey: BaseKey = SplashKey()
    override val containerId: Int = R.id.container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
