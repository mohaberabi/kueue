package com.mohaberabi.kline.decoder

import androidx.compose.ui.graphics.ImageBitmap

interface EscGsv0Decoder {

    suspend fun decode(bytes: ByteArray): ImageBitmap?
}