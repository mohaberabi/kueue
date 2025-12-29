package com.mohaberabi.kline.di

import com.mohaberabi.kline.decoder.PlatformBitmap
import com.mohaberabi.kline.decoder.SkiaPlatformBitmap
import com.mohaberabi.kline.server.KtorNativeSocketServer
import com.mohaberabi.kline.server.NativeSocketServer
import com.mohaberabi.kline.utility.IpAddressProvider
import com.mohaberabi.klines.client.KtorSocketClient
import com.mohaberabi.klines.client.NativeSocketClient
import com.mohaberabi.klines.media.model.AppMediaPlayer
import com.mohaberabi.klines.media.model.IosAppMediaPlayer
import com.mohaberabi.utility.IosIpAddressProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<NativeSocketServer> {
        KtorNativeSocketServer(get(), Dispatchers.IO)
    }
    single<PlatformBitmap> { SkiaPlatformBitmap(Dispatchers.Default) }
    single<NativeSocketClient> { KtorSocketClient(Dispatchers.IO) }
    single<IpAddressProvider> { IosIpAddressProvider(Dispatchers.IO) }
    single<AppMediaPlayer> { IosAppMediaPlayer() }
}
