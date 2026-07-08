package com.mista.weather.splash.presentation

import androidx.fragment.app.Fragment
import com.mista.weather.base.BaseKey
import com.mista.weather.base.NavigationAnimation
import kotlinx.parcelize.Parcelize

@Parcelize
data class SplashKey(private val id: Int = 0) : BaseKey() {
    override val animation: NavigationAnimation get() = NavigationAnimation.Fade
    override fun instantiateFragment(): Fragment = SplashFragment()
}
