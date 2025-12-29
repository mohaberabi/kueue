package com.mohaberabi.kline.server

sealed interface StartServerResult {
    data object ServerStarted : StartServerResult
    data object ServerAlreadyStarted : StartServerResult
    data object NoHostAssociated : StartServerResult
    data class Error(val error: Throwable) : StartServerResult
}