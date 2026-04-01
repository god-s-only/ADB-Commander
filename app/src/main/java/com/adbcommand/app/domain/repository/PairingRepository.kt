package com.adbcommand.app.domain.repository

import com.adbcommand.app.core.PairingResult
import com.adbcommand.app.domain.models.PairingCredentials

interface PairingRepository {
    suspend fun pairDevice(credentials: PairingCredentials): PairingResult
    suspend fun connectDevice(ipAddress: String, adbPort: Int): Result<String>
    suspend fun disconnectDevice(ipAddress: String, adbPort: Int): Result<String>
}