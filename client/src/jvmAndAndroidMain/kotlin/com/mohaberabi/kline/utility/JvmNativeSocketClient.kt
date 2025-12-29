package com.mohaberabi.kline.utility

import com.mohaberabi.klines.client.NativeSocketClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.Socket

class JvmNativeSocketClient(
    private val ioDispatcher: CoroutineDispatcher
) : NativeSocketClient {
    override suspend fun connectAndSend(
        ip: String,
        port: Int,
        bytes: ByteArray
    ) {
        withContext(ioDispatcher) {
            Socket(ip, port).use {
                val out = it.getOutputStream()
                out.use { op ->
                    op.write(bytes)
                    op.flush()
                }
            }
        }

    }
}