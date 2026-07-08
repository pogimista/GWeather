package com.mista.weather.home.presentation

import androidx.lifecycle.viewModelScope
import com.mista.weather.base.BaseViewModel
import com.mista.weather.base.BaseViewModelDeps
import com.mista.weather.base.error.toAppError
import com.mista.weather.home.data.WeatherHistoryRepository
import com.mista.weather.home.domain.GetCurrentWeatherUseCase
import com.mista.weather.home.domain.WeatherHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    deps: BaseViewModelDeps,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val weatherHistoryRepository: WeatherHistoryRepository,
) : BaseViewModel(deps) {

    private val _weatherState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val weatherState = _weatherState.asStateFlow()

    private val _historyState = MutableStateFlow<List<WeatherHistoryEntry>>(weatherHistoryRepository.getHistory())
    val historyState = _historyState.asStateFlow()

    private var lastLat: Double = DEFAULT_LAT
    private var lastLon: Double = DEFAULT_LON

    init {
        loadWeather()
    }

    fun loadWeather(lat: Double = lastLat, lon: Double = lastLon) {
        lastLat = lat
        lastLon = lon
        viewModelScope.launch {
            _weatherState.value = HomeUiState.Loading
            getCurrentWeatherUseCase(GetCurrentWeatherUseCase.Params(lat = lat, lon = lon))
                .onSuccess { weather ->
                    _weatherState.value = HomeUiState.Success(weather)
                    weatherHistoryRepository.addEntry(weather)
                    _historyState.value = weatherHistoryRepository.getHistory()
                }
                .onFailure { error -> _weatherState.value = HomeUiState.Error(getErrorMessage(error.toAppError())) }
        }
    }

    fun retry() = loadWeather()

    private companion object {
        const val DEFAULT_LAT = 44.34
        const val DEFAULT_LON = 10.99
    }
}
