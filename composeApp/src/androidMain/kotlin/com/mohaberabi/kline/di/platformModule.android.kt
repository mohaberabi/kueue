package com.mohaberabi.kline.di

import com.mohaberabi.kline.decoder.AndroidPlatformBitmap
import com.mohaberabi.kline.decoder.PlatformBitmap
import com.mohaberabi.kline.media.AndroidAppMediaPlayer
import com.mohaberabi.kline.utility.IpAddressProvider
import com.mohaberabi.kline.utility.JvmIpProvider
import com.mohaberabi.kline.utility.JvmNativeSocketClient
import com.mohaberabi.klines.client.KtorSocketClient
import com.mohaberabi.klines.client.NativeSocketClient
import com.mohaberabi.klines.media.model.AppMediaPlayer
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
//    single<NativeSocketClient> {
//       JvmNativeSocketClient(Dispatchers.IO)
//    }
    single<PlatformBitmap> {
        AndroidPlatformBitmap(Dispatchers.Default)
    }
    single<NativeSocketClient> { KtorSocketClient(Dispatchers.IO) }
    single<IpAddressProvider> { JvmIpProvider(Dispatchers.IO) }
    singleOf(::AndroidAppMediaPlayer) bind AppMediaPlayer::class
}