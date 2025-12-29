package com.mohaberabi.kline

import android.app.Application
import com.mohaberabi.kline.di.initKoin
import org.koin.android.ext.koin.androidContext

class KlineApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@KlineApplication)
        }
    }
}