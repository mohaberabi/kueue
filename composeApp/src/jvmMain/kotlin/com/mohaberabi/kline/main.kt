package com.mohaberabi.kline

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mohaberabi.kline.di.initKoin


fun main() = application() {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kline",
    ) {
        KlineApp()
    }
}

