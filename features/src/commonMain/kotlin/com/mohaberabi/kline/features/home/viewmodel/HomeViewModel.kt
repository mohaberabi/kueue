package com.mohaberabi.kline.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohaberabi.kline.server.DEFAULT_PORT
import com.mohaberabi.kline.server.NativeSocketServer
import com.mohaberabi.kline.server.StartServerResult
import com.mohaberabi.kline.utility.IpAddressProvider
import com.mohaberabi.klines.client.NativeSocketClient
import com.mohaberabi.klines.media.model.AppMediaPlayer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val client: NativeSocketClient,
    private val server: NativeSocketServer,
    private val ipAddressProvider: IpAddressProvider,
    private val handleReceivedPrintJob: HandleReceivedPrintJobUseCase,
    private val audioPlayer: AppMediaPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    private val _events = Channel<HomeEvents>()
    val events = _events.receiveAsFlow()


    fun onAction(action: HomeActions) {
        when (action) {
            HomeActions.OnTestPrint -> sendTest()
            HomeActions.OnToggleServer -> toggleServer()
            HomeActions.ToggleInfoSheet -> toggleSheet()
        }
    }

    private fun toggleSheet() {
        val isShown = _state.value.showInfoSheet
        if (isShown.not()) {
            viewModelScope.launch {
                runCatching {
                    ipAddressProvider.getIpAddress()
                }.onSuccess { ip ->
                    _state.update { it.copy(ipAddress = ip ?: "", showInfoSheet = true) }
                }
            }
        } else {
            _state.update { it.copy(showInfoSheet = false) }
        }
    }

    private val collectPrintJobs = server
        .outputData
        .onEach { bytes ->
            val content = handleReceivedPrintJob(bytes)
            _state.update { it.copy(receivedContent = it.receivedContent + content) }
            audioPlayer.play(createPrinterSound())
        }
        .catch {
            sendError(it.message)
            stopServer()
        }.launchIn(viewModelScope)


    private fun toggleServer() {
        if (_state.value.serverRunning) stopServer() else startServer()
    }

    private fun stopServer() {
        server.stop()
        _state.update { it.copy(serverRunning = false) }
    }


    private fun startServer() {
        viewModelScope.launch {
            runCatching {
                server.start()
            }.onSuccess { result ->
                when (result) {
                    StartServerResult.ServerAlreadyStarted,
                    StartServerResult.ServerStarted -> _state.update { it.copy(serverRunning = true) }

                    is StartServerResult.Error -> sendError(result.error.message)
                    StartServerResult.NoHostAssociated -> sendError("Not connected to network")
                }
            }.onFailure { sendError(it.message) }
        }
    }

    private fun sendTest() {
        viewModelScope.launch {
            val ipAddress = ipAddressProvider.getIpAddress()
                ?: run { sendError("Not connected to network"); return@launch; }
            runCatching {
                val message = """
                    SimulatedPrinter
                    IP Address : ${ipAddress}
                    Port       : ${DEFAULT_PORT}
                    """.trimIndent() + "\n"
                val bytes = message.encodeToByteArray()
                client.connectAndSend(
                    ipAddress,
                    DEFAULT_PORT,
                    bytes,
                )
            }.onFailure { sendError(it.message) }
        }
    }

    private suspend fun sendError(message: String?) {
        _events.send(HomeEvents.Error(message ?: "Something Went wrong"))
    }
}