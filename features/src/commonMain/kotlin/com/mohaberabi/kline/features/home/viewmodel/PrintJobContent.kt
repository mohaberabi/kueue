package com.mohaberabi.kline.features.home.viewmodel

import androidx.compose.ui.graphics.ImageBitmap

sealed interface PrintJobContent {
    data object None : PrintJobContent
    data class PLainText(val text: String) : PrintJobContent
    class RasterImage(val bitmap: ImageBitmap) : PrintJobContent
}