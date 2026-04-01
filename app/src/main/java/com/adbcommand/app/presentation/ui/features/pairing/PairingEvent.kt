package com.adbcommand.app.presentation.ui.features.pairing

sealed class PairingEvent {
    data class IpChanged(val value: String): PairingEvent()
    data class PortChanged(val value: String): PairingEvent()
    data class CodeChanged(val value: String): PairingEvent()
    object StartPairing: PairingEvent()
    object Reset: PairingEvent()
}