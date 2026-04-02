package com.adbcommand.app.domain.models

data class DeviceInfo(
    val ip: String,
    val ipError: String?,
    val pairingPort: String,
    val pairingPortError: String?,
    val adbPort: String
)