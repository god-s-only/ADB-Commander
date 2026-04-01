package com.adbcommand.app.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.PairingResult
import com.adbcommand.app.domain.models.PairingCredentials
import com.adbcommand.app.domain.usecase.home.ConnectAdbDeviceUseCase
import com.adbcommand.app.domain.usecase.home.PairAdbDeviceUseCase
import com.adbcommand.app.presentation.ui.features.pairing.PairingEvent
import com.adbcommand.app.presentation.ui.features.pairing.PairingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
enum class PairingStep { IDLE, PAIRING, CONNECTING, SUCCESS, ERROR }

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val pairDevice: PairAdbDeviceUseCase,
    private val connectDevice: ConnectAdbDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()


    fun onEvent(event: PairingEvent) {
        when (event) {
            is PairingEvent.IpChanged   -> _uiState.update {
                it.copy(ipAddress = event.value, ipError = null)
            }
            is PairingEvent.PortChanged -> _uiState.update {
                it.copy(port = event.value, portError = null)
            }
            is PairingEvent.CodeChanged -> {
                val filtered = event.value.filter { it.isDigit() }.take(6)
                _uiState.update { it.copy(pairingCode = filtered, codeError = null) }
            }
            is PairingEvent.StartPairing -> attemptPairing()
            is PairingEvent.Reset        -> _uiState.value = PairingUiState()
        }
    }


    private fun validate(state: PairingUiState): Boolean {
        val ipError = when {
            state.ipAddress.isBlank()  -> "IP address is required"
            !isValidIpAddress(state.ipAddress) -> "Enter a valid IPv4 address"
            else -> null
        }
        val portError = when {
            state.port.isBlank() -> "Port is required"
            state.port.toIntOrNull() == null -> "Port must be a number"
            state.port.toInt() !in 1..65535 -> "Port must be 1–65535"
            else -> null
        }
        val codeError = when {
            state.pairingCode.length != 6 -> "Enter the full 6-digit code"
            else -> null
        }

        return if (ipError != null || portError != null || codeError != null) {
            _uiState.update { it.copy(ipError = ipError, portError = portError, codeError = codeError) }
            false
        } else true
    }

    private fun isValidIpAddress(ip: String): Boolean {
        val parts = ip.trim().split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
    }

    private fun attemptPairing() {
        val state = _uiState.value
        if (!validate(state)) return

        val credentials = PairingCredentials(
            ipAddress  = state.ipAddress.trim(),
            port = state.port.trim().toInt(),
            pairingCode = state.pairingCode.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(step = PairingStep.PAIRING) }

            when (val pairingResult = pairDevice(credentials)) {

                is PairingResult.Success -> {
                    _uiState.update { it.copy(step = PairingStep.CONNECTING) }

                    val connectResult = connectDevice(
                        credentials.ipAddress,
                        pairingResult.adbPort
                    )

                    if (connectResult.isSuccess) {
                        _uiState.update {
                            it.copy(
                                step = PairingStep.SUCCESS,
                                connectedAddress = "${credentials.ipAddress}:${pairingResult.adbPort}"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                step  = PairingStep.ERROR,
                                errorMessage = "Paired, but adb connect failed:\n" +
                                        (connectResult.exceptionOrNull()?.message ?: "Unknown error")
                            )
                        }
                    }
                }

                is PairingResult.InvalidCode -> _uiState.update {
                    it.copy(
                        step  = PairingStep.ERROR,
                        errorMessage = "Wrong pairing code — check the 6-digit code on your device and try again."
                    )
                }

                is PairingResult.NetworkError -> _uiState.update {
                    it.copy(
                        step  = PairingStep.ERROR,
                        errorMessage = "Network error: ${pairingResult.message}"
                    )
                }

                is PairingResult.UnknownError -> _uiState.update {
                    it.copy(
                        step = PairingStep.ERROR,
                        errorMessage = "Unexpected error: ${pairingResult.message}"
                    )
                }
            }
        }
    }
}