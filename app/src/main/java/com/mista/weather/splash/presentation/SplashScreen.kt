package com.mista.weather.splash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mista.weather.R
import com.mista.weather.ui.components.BaseScreen
import com.mista.weather.ui.theme.AppColors
import com.mista.weather.ui.theme.AppFonts
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val colors = AppColors.colors

    BaseScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.primary, colors.secondary))),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "⛅", fontSize = 72.sp)
            Text(
                text = stringResource(R.string.app_name),
                style = AppFonts.bold.copy(color = colors.white, fontSize = 32.sp),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
