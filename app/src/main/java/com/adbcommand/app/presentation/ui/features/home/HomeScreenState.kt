package com.adbcommand.app.presentation.ui.features.home

data class HomeScreenState(
    val ip: String           = "",
    val pairingPort: String  = "",
    val adbPort: String      = "5555",
    val pairingCode: String  = "",

    val isLoadingInfo: Boolean        = false,
    val isGeneratingCode: Boolean     = false,
    val isTestingConnection: Boolean  = false,

    val infoError: String?       = null,
    val codeMessage: String?     = null,
    val connectionStatus: ConnectionStatus? = null,

    val pairCommand: String    = "",
    val connectCommand: String = ""
)