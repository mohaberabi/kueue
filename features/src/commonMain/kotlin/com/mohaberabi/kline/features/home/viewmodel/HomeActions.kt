package com.mohaberabi.kline.features.home.viewmodel

sealed interface HomeActions {
    data object OnToggleServer : HomeActions
    data object OnTestPrint : HomeActions


    data object ToggleInfoSheet : HomeActions

}