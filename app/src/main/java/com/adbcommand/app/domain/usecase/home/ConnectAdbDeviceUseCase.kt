package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.domain.repository.PairingRepository
import jakarta.inject.Inject

class ConnectAdbDeviceUseCase @Inject constructor(
    private val repository: PairingRepository
) {
    suspend operator fun invoke(ipAddress: String, adbPort: Int): Result<String> =
        repository.connectDevice(ipAddress, adbPort)
}