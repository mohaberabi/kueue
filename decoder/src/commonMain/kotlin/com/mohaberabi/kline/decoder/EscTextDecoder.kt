package com.mohaberabi.kline.decoder

interface EscTextDecoder {

    suspend fun tryToDecodeText(
        bytes: ByteArray,
    ): String
}