package com.mohaberabi.klines.client


import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.core.use
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


class KtorSocketClient(
    private val ioDispatcher: CoroutineDispatcher,
) : NativeSocketClient {
    private val selector by lazy { SelectorManager(ioDispatcher) }
    override suspend fun connectAndSend(
        ip: String,
        port: Int,
        bytes: ByteArray,
    ): Unit = withContext(ioDispatcher) {
        aSocket(selector).tcp().connect(InetSocketAddress(ip, port)).use { socket ->
            val out = socket.openWriteChannel(autoFlush = false)
            out.writeFully(bytes, 0, bytes.size)
            out.flush()
            out.close()
        }
    }

}