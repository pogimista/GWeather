package com.mista.weather.home.presentation

import com.mista.weather.home.domain.Weather

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object PermissionRequired : HomeUiState
    data class Success(val weather: Weather) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
