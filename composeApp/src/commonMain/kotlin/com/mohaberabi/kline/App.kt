package com.mohaberabi.kline

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.mohaberabi.kline.features.home.screen.HomeScreen


@Composable
fun KlineApp() {
    MaterialTheme {
        HomeScreen()
    }

}

