package com.mohaberabi.kline.features.home.viewmodel

import com.mohaberabi.klines.media.model.PlatformMediaResource

actual fun createPrinterSound(): PlatformMediaResource {
    return PlatformMediaResource("/raw/printer.mp3")
}