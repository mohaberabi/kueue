package com.mohaberabi.kline.di

import com.mohaberabi.kline.features.di.featuresModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


fun initKoin(
    declaration: KoinAppDeclaration? = null
) {
    startKoin {
        declaration?.invoke(this)
        modules(
            appModule,
            featuresModule,
        )
    }

}