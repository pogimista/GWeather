package com.mista.weather.home.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mista.weather.R
import com.mista.weather.ui.components.BaseScreen
import com.mista.weather.ui.theme.AppColors
import com.mista.weather.ui.theme.AppFonts
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val colors = AppColors.colors
    val context = LocalContext.current
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsStateWithLifecycle()

    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var canShowRationale by rememberSaveable { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionRequested = true
        if (!granted) {
            canShowRationale = context.findActivity()?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } ?: true
        }
        viewModel.onLocationPermissionResult(granted)
    }
    val permanentlyDenied = permissionRequested && !locationPermissionGranted && !canShowRationale

    val tabTitles = listOf(
        stringResource(R.string.home_tab_current),
        stringResource(R.string.home_tab_history),
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val coroutineScope = rememberCoroutineScope()

    BaseScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Text(
                text = stringResource(R.string.home_placeholder_title),
                style = AppFonts.bold.copy(color = colors.textPrimary, fontSize = 28.sp),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = colors.background,
                contentColor = colors.primary,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = title, style = AppFonts.medium) },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> CurrentWeatherTab(
                        state = weatherState,
                        onRetry = viewModel::retry,
                        showLocationPrompt = !locationPermissionGranted,
                        permanentlyDenied = permanentlyDenied,
                        onRequestLocation = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        onOpenSettings = { context.startActivity(locationSettingsIntent(context.packageName)) },
                    )

                    else -> WeatherHistoryTab(entries = historyState)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_log_in),
                    style = AppFonts.medium.copy(color = colors.primary),
                    modifier = Modifier.clickable { viewModel.showToastMessage(R.string.action_coming_soon) },
                )
                Text(
                    text = "  •  ",
                    style = AppFonts.regular.copy(color = colors.textDisabled),
                )
                Text(
                    text = stringResource(R.string.home_create_account),
                    style = AppFonts.medium.copy(color = colors.primary),
                    modifier = Modifier.clickable { viewModel.showToastMessage(R.string.action_coming_soon) },
                )
            }
        }
    }
}
