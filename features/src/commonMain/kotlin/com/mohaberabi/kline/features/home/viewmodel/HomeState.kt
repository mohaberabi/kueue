package com.mohaberabi.kline.features.home.viewmodel

data class HomeState(
    val receivedContent: List<PrintJobContent> = listOf(PrintJobContent.None),
    val serverRunning: Boolean = false,
    val showInfoSheet: Boolean = false,
    val ipAddress: String = ""
)