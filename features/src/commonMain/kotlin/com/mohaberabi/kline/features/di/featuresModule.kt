package com.mohaberabi.kline.features.di

import com.mohaberabi.kline.features.home.viewmodel.HandleReceivedPrintJobUseCase
import com.mohaberabi.kline.features.home.viewmodel.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val featuresModule = module {
    viewModelOf(::HomeViewModel)
    factoryOf(::HandleReceivedPrintJobUseCase)
}