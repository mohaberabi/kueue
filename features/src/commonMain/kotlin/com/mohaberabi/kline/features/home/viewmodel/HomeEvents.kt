package com.mohaberabi.kline.features.home.viewmodel


sealed interface HomeEvents {
    data class Error(val message: String) : HomeEvents
}