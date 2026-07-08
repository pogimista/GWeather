package com.mista.weather.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mista.weather.R
import com.mista.weather.home.domain.Weather
import com.mista.weather.ui.components.BaseScreen
import com.mista.weather.ui.theme.AppColors
import com.mista.weather.ui.theme.AppFonts
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val colors = AppColors.colors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BaseScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_placeholder_title),
                style = AppFonts.semiBold.copy(color = colors.textPrimary),
            )

            when (val state = uiState) {
                is HomeUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.padding(top = 24.dp),
                    color = colors.primary,
                )

                is HomeUiState.Success -> WeatherContent(weather = state.weather)

                is HomeUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                )
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather) {
    val colors = AppColors.colors

    Column(
        modifier = Modifier.padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (weather.country != null) {
                stringResource(R.string.home_location, weather.cityName, weather.country)
            } else {
                weather.cityName
            },
            style = AppFonts.semiBold.copy(color = colors.textPrimary),
        )
        Text(
            text = stringResource(R.string.home_temperature, weather.temperature),
            style = AppFonts.bold.copy(color = colors.textPrimary, fontSize = 48.sp),
        )
        Text(
            text = weather.description.replaceFirstChar { it.uppercase() },
            style = AppFonts.medium.copy(color = colors.textSecondary),
        )
        Text(
            text = stringResource(R.string.home_feels_like, weather.feelsLike),
            style = AppFonts.regular.copy(color = colors.textSecondary),
        )
        Text(
            text = stringResource(R.string.home_humidity, weather.humidity),
            style = AppFonts.regular.copy(color = colors.textSecondary),
        )
        Text(
            text = stringResource(R.string.home_wind, weather.windSpeed),
            style = AppFonts.regular.copy(color = colors.textSecondary),
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    val colors = AppColors.colors

    Column(
        modifier = Modifier.padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = AppFonts.regular.copy(color = colors.danger, textAlign = TextAlign.Center),
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}
