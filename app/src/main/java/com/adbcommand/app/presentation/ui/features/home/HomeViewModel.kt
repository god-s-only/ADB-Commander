package com.adbcommand.app.presentation.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.Commands
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.usecase.home.GetPairingCodeUseCase
import com.adbcommand.app.domain.usecase.home.LoadDeviceInfoUseCase
import com.adbcommand.app.domain.usecase.home.TestConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ConnectionStatus { SUCCESS, FAILURE }

data class ConnectionResult(val status: ConnectionStatus, val message: String)
data class ShizukuState(
    val isRunning: Boolean = false,
    val isPermissionGranted: Boolean = false
) {
    val isFullyAvailable: Boolean get() = isRunning && isPermissionGranted
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loadDeviceInfo: LoadDeviceInfoUseCase,
    private val getPairingCode: GetPairingCodeUseCase,
    private val testConnection: TestConnectionUseCase,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuManager.state

    init {
        onEvent(HomeEvent.LoadInfo)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadInfo -> loadInfo()
            is HomeEvent.GenerateCode -> generateCode()
            is HomeEvent.TestConnection -> runConnectionTest()
            is HomeEvent.DismissStatus -> _uiState.update { it.copy(connectionStatus = null) }
            is HomeEvent.RequestShizukuPermission -> shizukuManager.requestPermission()
        }
    }

    private fun loadInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInfo = true, infoError = null) }

            val info = loadDeviceInfo()

            val pairCmd = if (info.ip.isNotBlank() && info.pairingPort.isNotBlank())
                Commands.adbPairCommand(info.ip, info.pairingPort, _uiState.value.pairingCode)
            else ""

            val connectCmd = if (info.ip.isNotBlank())
                Commands.adbConnectCommand(info.ip, info.adbPort)
            else ""

            _uiState.update {
                it.copy(
                    isLoadingInfo = false,
                    ip = info.ip,
                    pairingPort = info.pairingPort,
                    adbPort = info.adbPort,
                    infoError = info.ipError ?: info.pairingPortError,
                    pairCommand = pairCmd,
                    connectCommand = connectCmd
                )
            }
        }
    }
    private fun generateCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, codeMessage = null) }

            val result = getPairingCode()

            result.fold(
                onSuccess = { code ->
                    val state = _uiState.value
                    val pairCmd = if (state.ip.isNotBlank() && state.pairingPort.isNotBlank())
                        Commands.adbPairCommand(state.ip, state.pairingPort, code)
                    else ""

                    _uiState.update {
                        it.copy(
                            isGeneratingCode = false,
                            pairingCode = code,
                            codeMessage = null,
                            pairCommand = pairCmd
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingCode = false,
                            codeMessage = error.message
                                ?: "Read the code from Settings › Wireless Debugging"
                        )
                    }
                }
            )
        }
    }
    private fun runConnectionTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionStatus = null) }
            val result = testConnection()
            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    connectionStatus = if (result.isSuccess)
                        ConnectionStatus.SUCCESS
                    else
                        ConnectionStatus.FAILURE
                )
            }
        }
    }
}