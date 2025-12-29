package com.mohaberabi.kline.di

import com.mohaberabi.kline.decoder.DefaultEscGsv0Decoder
import com.mohaberabi.kline.decoder.DefaultEscTextDecoder
import com.mohaberabi.kline.decoder.EscGsv0Decoder
import com.mohaberabi.kline.decoder.EscTextDecoder
import com.mohaberabi.kline.server.KtorNativeSocketServer
import com.mohaberabi.kline.server.NativeSocketServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module


val appModule = module {
    includes(platformModule)
    single<EscTextDecoder> { DefaultEscTextDecoder(Dispatchers.Default) }
    single<EscGsv0Decoder> { DefaultEscGsv0Decoder(get(), Dispatchers.Default) }
    single<NativeSocketServer> { KtorNativeSocketServer(get(), Dispatchers.IO) }
}