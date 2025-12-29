package com.mohaberabi.kline

import androidx.compose.ui.window.ComposeUIViewController
import com.mohaberabi.kline.decoder.SkiaPlatformBitmap
import com.mohaberabi.kline.di.initKoin
import com.mohaberabi.kline.server.DEFAULT_PORT
import com.mohaberabi.kline.server.KtorNativeSocketServer
import com.mohaberabi.utility.IosIpAddressProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

private val bonjour = BonjourPublisher(DEFAULT_PORT)

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
        bonjour.start()
    }
) {
    KlineApp()
}

