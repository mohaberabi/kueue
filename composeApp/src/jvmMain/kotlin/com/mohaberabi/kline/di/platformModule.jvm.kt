package com.mohaberabi.kline.di

import com.mohaberabi.kline.decoder.PlatformBitmap
import com.mohaberabi.kline.decoder.SkiaPlatformBitmap
import com.mohaberabi.kline.server.KtorNativeSocketServer
import com.mohaberabi.kline.server.NativeSocketServer
import com.mohaberabi.kline.utility.IpAddressProvider
import com.mohaberabi.kline.utility.JvmIpProvider
import com.mohaberabi.kline.utility.JvmNativeSocketClient
import com.mohaberabi.klines.client.KtorSocketClient
import com.mohaberabi.klines.client.NativeSocketClient
import com.mohaberabi.klines.media.model.AppMediaPlayer
import com.mohaberabi.klines.media.model.JvmAppAudiPlayer
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<NativeSocketClient> {
        JvmNativeSocketClient(Dispatchers.IO)
    }
    single<NativeSocketServer> {
        KtorNativeSocketServer(get(), Dispatchers.IO)
    }
    single<PlatformBitmap> { SkiaPlatformBitmap(Dispatchers.Default) }
    single<NativeSocketClient> { KtorSocketClient(Dispatchers.IO) }
    single<IpAddressProvider> { JvmIpProvider(Dispatchers.IO) }
    single<AppMediaPlayer> { JvmAppAudiPlayer() }

}