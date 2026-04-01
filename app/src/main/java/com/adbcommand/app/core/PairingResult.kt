package com.adbcommand.app.core

sealed class PairingResult {
    data class Success(val adbPort: Int = 5555) : PairingResult()
    object InvalidCode : PairingResult()
    data class NetworkError(val message: String) : PairingResult()
    data class UnknownError(val message: String) : PairingResult()
}