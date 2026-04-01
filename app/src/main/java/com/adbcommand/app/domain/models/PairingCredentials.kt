package com.adbcommand.app.domain.models

data class PairingCredentials(
    val ipAddress: String,
    val port: Int,
    val pairingCode: String
)