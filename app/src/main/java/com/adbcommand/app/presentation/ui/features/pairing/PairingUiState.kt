package com.adbcommand.app.presentation.ui.features.pairing

import com.adbcommand.app.presentation.pairing.PairingStep

data class PairingUiState(
    val ipAddress: String        = "",
    val port: String             = "",
    val pairingCode: String      = "",
    val step: PairingStep        = PairingStep.IDLE,
    val connectedAddress: String = "",
    val errorMessage: String     = "",

    val ipError: String?         = null,
    val portError: String?       = null,
    val codeError: String?       = null,
)