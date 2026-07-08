package com.mista.weather.base

sealed class NavigationEvent {

    data class To(val key: BaseKey) : NavigationEvent()

    object Back : NavigationEvent()

    data class BackTo(val key: BaseKey, val inclusive: Boolean = false) : NavigationEvent()

    object BackToRoot : NavigationEvent()

    data class ReplaceAll(val key: BaseKey) : NavigationEvent()
}
