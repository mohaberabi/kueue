package com.mohaberabi.kline.server

import com.mohaberabi.kline.utility.BytesOutputStream
import com.mohaberabi.kline.utility.CUT_FULL
import com.mohaberabi.kline.utility.CUT_PARTIAL_PREFIX
import com.mohaberabi.kline.utility.IpAddressProvider
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Starts and starts listen as a TCP ServerSocket to simulate how printers works exactly on a low level
 * we start by creating a [ServerSocket] to keep listening on the ip and port provided then reacts to each received bytes
 * in a connection it also handles if 1 connection keeps opened and sending multiple bytes , like printers when they receive
 * multiple print jobs at once [print this paper 10 times ] is a high level example
 * @property outputChannel a [Buffered Channel] to hold and send the received Bytes
 * @property job a single [kotlinx.coroutines.Job] to control server processing
 * @property selector
 * @property serverSocket
 */
class KtorNativeSocketServer(
    private val ipAddressProvider: IpAddressProvider,
    private val ioDispatcher: CoroutineDispatcher
) : NativeSocketServer {

    private val outputChannel = Channel<ByteArray>(64)
    override val outputData: Flow<ByteArray> = outputChannel.receiveAsFlow()

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())
    private var job: Job? = null
    private var selector: SelectorManager? = null
    private var serverSocket: ServerSocket? = null

    /**
     * starts our server and keeps listening
     * if [job] is still active -> return [StartServerResult.ServerAlreadyStarted]
     * if [ipAddress ] not available return [StartServerResult.NoHostAssociated]
     * start initializing server instance and bind it to [AnyNetworkInterface] with [DEFAULT_PORT]
     * keep looping while the [Job.isActive]
     * [ServerSocket.accept] accepts a connection between the server and clients  Suspends until a connection is available
     * once a connection is received delegate it to another coroutine to handle it
     * we use the socket to be autoclosed once we are dong processing
     *
     */
    override suspend fun start(): StartServerResult {
        if (job?.isActive == true) return StartServerResult.ServerAlreadyStarted
        clean()
        val ip = ipAddressProvider.getIpAddress() ?: return StartServerResult.NoHostAssociated
        job = scope.launch {
            val server = initServer()
            serverLoop@ while (isActive) {
                /** [Socket.receiveChannel] is channel that stores the received bytes
                 * [buffer] is byteArray to read available bytes from the [receiveChannel] into it
                 * if [read] == -1 -> means connection closed break the loop
                 * [sink] Accumulates bytes - Grows dynamically  - Allows searching & cutting see [BytesOutputStream]
                 *
                 */
                val socket = server.accept()
                launch {
                    socket.use {
                        val receiveChannel = it.openReadChannel()
                        val buffer = ByteArray(4096)
                        val sink = BytesOutputStream(8 * 1024)
                        connectionLoop@ while (true) {
                            val read = receiveChannel.readAvailable(buffer, 0, buffer.size)
                            if (read == -1) break@connectionLoop
                            if (read <= 0) continue@connectionLoop
                            /** Adds new bytes after previous bytes.*/
                            sink.write(buffer, 0, read)
                            /** we need to maintain the connection reading as client can keep sending in 1 instance of connection*/
                            jobScanLoop@ while (true) {
                                /** search for a full index cut
                                 *  and if found means this bulk of bytes we have until now needs to be printed together
                                 *  means the upcoming new parts are independent from the last previously sent so we need to emit it
                                 */
                                val fullIndex = sink.indexOfPattern(CUT_FULL)
                                if (fullIndex != -1) {
                                    val end = fullIndex + CUT_FULL.size
                                    val job = sink.popFromTop(end)
                                    sendIfNotEmpty(job)
                                    continue@jobScanLoop
                                }
                                /**
                                Imagine instead of bytes we are working with STRINGS.
                                CUT_PARTIAL_PREFIX = "CUT("
                                So the FULL command should look like:
                                "CUT(3)"   ← where 3 is the parameter byte
                                Now imagine the data arrives over the network in pieces.
                                 */
                                val partialIndex = sink.indexOfPattern(CUT_PARTIAL_PREFIX)
                                if (partialIndex != -1) {
                                    /**
                                    Example sink content (what we received so far):
                                    Case 1 (INCOMPLETE):
                                    sink = "Hello World CUT("
                                    Case 2 (COMPLETE):
                                    sink = "Hello World CUT(3)"
                                    end means:
                                    - start of "CUT("
                                    - plus length of "CUT("
                                    - plus ONE extra character (the parameter '3')
                                    We are saying:
                                    "Do we already have the full CUT command?"
                                     */
                                    val end = partialIndex + CUT_PARTIAL_PREFIX.size + 1
                                    if (sink.size() >= end) {
                                        /**
                                        This means we DO have the full command.
                                        Example:
                                        sink = "Hello World CUT(3)"
                                        ^^^^^^^^^^
                                        So we can safely extract the whole receipt:
                                        "Hello World CUT(3)"
                                         */
                                        val job = sink.popFromTop(end)
                                        sendIfNotEmpty(job)
                                        continue@jobScanLoop
                                    } else {
                                        break@jobScanLoop
                                    }
                                }
                                break@jobScanLoop
                            }
                        }
                        val remainBytes = sink.toByteArray()
                        sendIfNotEmpty(remainBytes)
                    }
                }
            }
        }
        return StartServerResult.ServerStarted
    }

    private fun CoroutineScope.sendIfNotEmpty(bytes: ByteArray) {
        launch { outputChannel.send(bytes) }
    }

    private fun initServer(): ServerSocket {
        selector = SelectorManager(ioDispatcher)
        serverSocket = aSocket(requireNotNull(selector))
            .tcp()
            .bind("0.0.0.0", DEFAULT_PORT)
        return requireNotNull(serverSocket)
    }

    override fun stop() {
        clean()
    }


    private fun clean() {
        runCatching {
            job?.cancel()
            job = null
            serverSocket?.close()
            serverSocket = null
            selector?.close()
            selector = null
        }
    }


}
