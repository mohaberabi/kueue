package com.mohaberabi.kline.server

import kotlinx.coroutines.flow.Flow

interface NativeSocketServer {
    suspend fun start(): StartServerResult
    fun stop()
    val outputData: Flow<ByteArray>
}