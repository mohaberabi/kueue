package com.mohaberabi.klines.client

interface NativeSocketClient {

    suspend fun connectAndSend(
        ip: String,
        port: Int,
        bytes: ByteArray
    )
}