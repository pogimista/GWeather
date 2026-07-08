package com.mista.weather.home.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mista.weather.R
import com.mista.weather.home.domain.Weather
import com.mista.weather.ui.theme.AppColors
import com.mista.weather.ui.theme.AppFonts

@Composable
fun CurrentWeatherTab(
    state: HomeUiState,
    onRetry: () -> Unit,
    showOpenSettings: Boolean,
    onRequestLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.colors

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = state, label = "current_weather_state") { current ->
            when (current) {
                is HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }

                is HomeUiState.PermissionRequired -> LocationPermissionEmptyState(
                    showOpenSettings = showOpenSettings,
                    onEnableLocation = onRequestLocation,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.fillMaxSize(),
                )

                is HomeUiState.Success -> WeatherContent(
                    weather = current.weather,
                    modifier = Modifier.fillMaxSize(),
                )

                is HomeUiState.Error -> ErrorContent(
                    message = current.message,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun LocationPermissionEmptyState(
    showOpenSettings: Boolean,
    onEnableLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppColors.colors

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "📍", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_location_permission_title),
            style = AppFonts.semiBold.copy(color = colors.textPrimary, fontSize = 20.sp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_location_permission_message),
            style = AppFonts.regular.copy(color = colors.textSecondary),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = if (showOpenSettings) onOpenSettings else onEnableLocation,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
        ) {
            Text(
                text = stringResource(
                    if (showOpenSettings) R.string.home_location_open_settings else R.string.home_location_enable,
                ),
            )
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather, modifier: Modifier = Modifier) {
    val colors = AppColors.colors

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(colors.primary, colors.secondary)))
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (weather.country != null) {
                        stringResource(R.string.home_location, weather.cityName, weather.country)
                    } else {
                        weather.cityName
                    },
                    style = AppFonts.semiBold.copy(color = colors.white, fontSize = 18.sp),
                )
                Text(text = weatherEmoji(weather), fontSize = 56.sp)
                Text(
                    text = stringResource(R.string.home_temperature, weather.temperature),
                    style = AppFonts.bold.copy(color = colors.white, fontSize = 56.sp),
                )
                Text(
                    text = weather.description.replaceFirstChar { it.uppercase() },
                    style = AppFonts.medium.copy(color = colors.baseShadeWhite80),
                )
                Text(
                    text = stringResource(R.string.home_feels_like, weather.feelsLike),
                    style = AppFonts.regular.copy(color = colors.baseShadeWhite80),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WeatherStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.home_humidity_label),
                value = "${weather.humidity}%",
            )
            WeatherStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.home_wind_label),
                value = stringResource(R.string.home_wind_value, weather.windSpeed),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WeatherStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.home_sunrise_label),
                value = formatLocalTime(weather.sunrise, weather.timezoneOffsetSeconds),
                icon = "🌅",
            )
            WeatherStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.home_sunset_label),
                value = formatLocalTime(weather.sunset, weather.timezoneOffsetSeconds),
                icon = "🌇",
            )
        }
    }
}

@Composable
private fun WeatherStatCard(label: String, value: String, modifier: Modifier = Modifier, icon: String? = null) {
    val colors = AppColors.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.backgroundSecondary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = AppFonts.regular.copy(color = colors.textSecondary))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) Text(text = icon, fontSize = 18.sp)
            Text(text = value, style = AppFonts.semiBold.copy(color = colors.textPrimary, fontSize = 18.sp))
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppColors.colors

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = AppFonts.regular.copy(color = colors.danger, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}
