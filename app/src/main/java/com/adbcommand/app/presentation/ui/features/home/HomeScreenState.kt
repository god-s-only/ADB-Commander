package com.adbcommand.app.presentation.ui.features.home

enum class ConnectionStatus { SUCCESS, FAILURE }

data class HomeUiState(
    val ip: String = "",
    val adbPort: String = "5555",
    val pairingPort: String = "",
    val pairingCode: String = "",
    val pairCommand: String = "",
    val connectCommand: String = "",
    val isLoadingInfo: Boolean = false,
    val isGeneratingCode: Boolean = false,
    val isTestingConnection: Boolean = false,
    val infoError: String? = null,
    val codeMessage: String? = null,
    val connectionStatus: ConnectionStatus? = null,
    // QR screen trigger — set to true to navigate to QR screen
    val qrData: String? = null
)