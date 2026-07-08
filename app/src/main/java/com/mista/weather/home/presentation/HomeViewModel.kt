package com.mista.weather.home.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.mista.weather.R
import com.mista.weather.base.BaseUseCase
import com.mista.weather.base.BaseViewModel
import com.mista.weather.base.BaseViewModelDeps
import com.mista.weather.base.error.toAppError
import com.mista.weather.home.data.WeatherHistoryRepository
import com.mista.weather.home.domain.GetCurrentWeatherUseCase
import com.mista.weather.home.domain.GetDeviceLocationUseCase
import com.mista.weather.home.domain.WeatherHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    deps: BaseViewModelDeps,
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getDeviceLocationUseCase: GetDeviceLocationUseCase,
    private val weatherHistoryRepository: WeatherHistoryRepository,
) : BaseViewModel(deps) {

    private val _weatherState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val weatherState = _weatherState.asStateFlow()

    private val _historyState = MutableStateFlow<List<WeatherHistoryEntry>>(weatherHistoryRepository.getHistory())
    val historyState = _historyState.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(hasLocationPermission())
    val locationPermissionGranted = _locationPermissionGranted.asStateFlow()

    // Persisted across app restarts, so a permission denied in an earlier session is still
    // remembered - otherwise a fresh cold start would offer to re-prompt via the system dialog
    // even though the OS will silently no-op it, since it's already been asked before.
    private val _showOpenSettings = MutableStateFlow(
        cacheSession.locationPermissionRequested.value == true && !hasLocationPermission(),
    )
    val showOpenSettings = _showOpenSettings.asStateFlow()

    init {
        loadWeather()
    }

    fun retry() = loadWeather()

    fun onLocationPermissionResult(granted: Boolean) {
        cacheSession.locationPermissionRequested.value = true
        _locationPermissionGranted.value = granted
        _showOpenSettings.value = !granted
        loadWeather()
    }

    private fun loadWeather() {
        if (!hasLocationPermission()) {
            _weatherState.value = HomeUiState.PermissionRequired
            return
        }
        viewModelScope.launch {
            _weatherState.value = HomeUiState.Loading
            val coordinates = getDeviceLocationUseCase(BaseUseCase.NoParams).getOrNull()
            if (coordinates == null) {
                _weatherState.value = HomeUiState.Error(getLocalString(R.string.error_location_unavailable))
                return@launch
            }
            getCurrentWeatherUseCase(coordinates)
                .onSuccess { weather ->
                    _weatherState.value = HomeUiState.Success(weather)
                    weatherHistoryRepository.addEntry(weather)
                    _historyState.value = weatherHistoryRepository.getHistory()
                }
                .onFailure { error -> _weatherState.value = HomeUiState.Error(getErrorMessage(error.toAppError())) }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
