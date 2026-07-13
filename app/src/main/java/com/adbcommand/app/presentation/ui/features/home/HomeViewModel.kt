package com.adbcommand.app.presentation.ui.features.home

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.Commands
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.usecase.home.GetPairingCodeUseCase
import com.adbcommand.app.domain.usecase.home.LoadDeviceInfoUseCase
import com.adbcommand.app.domain.usecase.home.TestConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject


data class ConnectionResult(val status: ConnectionStatus, val message: String)
data class ShizukuState(
    val isRunning: Boolean = false,
    val isPermissionGranted: Boolean = false
) {
    val isFullyAvailable: Boolean get() = isRunning && isPermissionGranted
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuManager.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShizukuState())

    init {
        onEvent(HomeEvent.LoadInfo)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadInfo -> loadInfo()
            is HomeEvent.GenerateCode -> generateCode()
            is HomeEvent.TestConnection -> testConnection()
            is HomeEvent.DismissStatus -> _uiState.update { it.copy(connectionStatus = null) }
            is HomeEvent.RequestShizukuPermission -> shizukuManager.requestPermission()
            is HomeEvent.DismissQr -> _uiState.update { it.copy(qrData = null) }
        }
    }

    private fun loadInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInfo = true, infoError = null) }

            withContext(Dispatchers.IO) {
                val ip = getDeviceIp()
                val adbPort = readShizukuSetting("adb_wifi_port") ?: "5555"
                val pairingPort = readShizukuSetting("adb_wifi_pairing_port") ?: ""
                val pairingCode = readShizukuSetting("adb_wifi_pairing_code") ?: ""

                _uiState.update { state ->
                    state.copy(
                        ip = ip,
                        adbPort = adbPort.ifBlank { "5555" },
                        pairingPort = pairingPort,
                        pairingCode = pairingCode,
                        isLoadingInfo = false,
                        infoError = if (ip.isBlank()) "Could not detect IP — check Wi-Fi" else null,
                        pairCommand = buildPairCommand(ip, pairingPort, pairingCode),
                        connectCommand = buildConnectCommand(ip, adbPort),
                        codeMessage = when {
                            pairingCode.isNotBlank() -> null
                            else -> "Enable Wireless Debugging and open the pairing dialog, then tap Generate"
                        }
                    )
                }
            }
        }
    }

    private fun generateCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCode = true, codeMessage = null) }

            withContext(Dispatchers.IO) {
                val code = readShizukuSetting("adb_wifi_pairing_code")
                val port = readShizukuSetting("adb_wifi_pairing_port")
                val ip = _uiState.value.ip.ifBlank { getDeviceIp() }

                if (code.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isGeneratingCode = false,
                            codeMessage = "No pairing code found. Go to Developer Options → " +
                                    "Wireless Debugging → Pair device with pairing code, " +
                                    "then tap Generate again."
                        )
                    }
                    return@withContext
                }

                _uiState.update { state ->
                    state.copy(
                        isGeneratingCode = false,
                        pairingCode = code,
                        pairingPort = port ?: state.pairingPort,
                        pairCommand = buildPairCommand(ip, port ?: state.pairingPort, code),
                        connectCommand = buildConnectCommand(ip, state.adbPort),
                        codeMessage = null,
                        qrData = "WIFI:T:ADB;S:adbcommander-${ip.replace(".", "")};P:$code;;"
                    )
                }
            }
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionStatus = null) }

            withContext(Dispatchers.IO) {
                val reachable = try {
                    InetAddress.getByName("8.8.8.8").isReachable(3000)
                } catch (e: Exception) {
                    false
                }

                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionStatus = if (reachable) ConnectionStatus.SUCCESS
                        else ConnectionStatus.FAILURE
                    )
                }
            }
        }
    }

    private suspend fun readShizukuSetting(key: String): String? {
        return try {
            val result = shizukuManager.run("settings get global $key")
            val value = result.output.trim()
            if (value == "null" || value.isBlank()) null else value
        } catch (e: Exception) {
            Log.w("HomeViewModel", "Could not read $key", e)
            null
        }
    }

    private fun getDeviceIp(): String {
        return try {
            val wifi = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wifi.connectionInfo.ipAddress
            if (ip != 0) {
                String.format(
                    "%d.%d.%d.%d",
                    ip and 0xff,
                    ip shr 8 and 0xff,
                    ip shr 16 and 0xff,
                    ip shr 24 and 0xff
                )
            } else {
                NetworkInterface.getNetworkInterfaces()
                    ?.asSequence()
                    ?.flatMap { it.inetAddresses.asSequence() }
                    ?.firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(".") == true }
                    ?.hostAddress ?: ""
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "getDeviceIp failed", e)
            ""
        }
    }

    private fun buildPairCommand(ip: String, port: String, code: String): String {
        if (ip.isBlank() || port.isBlank() || code.isBlank()) return ""
        return "adb pair $ip:$port $code"
    }

    private fun buildConnectCommand(ip: String, port: String): String {
        if (ip.isBlank()) return ""
        return "adb connect $ip:${port.ifBlank { "5555" }}"
    }
}