package com.mista.weather.splash.presentation

import androidx.lifecycle.viewModelScope
import com.mista.weather.base.BaseViewModel
import com.mista.weather.base.BaseViewModelDeps
import com.mista.weather.home.presentation.HomeKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    deps: BaseViewModelDeps,
) : BaseViewModel(deps) {

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            navigateAsRoot(HomeKey())
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1200L
    }
}
