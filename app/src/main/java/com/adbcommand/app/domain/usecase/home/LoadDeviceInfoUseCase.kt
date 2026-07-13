package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.domain.models.DeviceInfo
import com.adbcommand.app.domain.repository.HomeRepository
import jakarta.inject.Inject

class LoadDeviceInfoUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): DeviceInfo {
        val ip          = repository.getDeviceIp()
        val pairingPort = repository.getPairingPort()
        val adbPort     = repository.getAdbPort()

        return DeviceInfo(
            ip          = ip.getOrNull() ?: "",
            ipError     = ip.exceptionOrNull()?.message,
            pairingPort = pairingPort.getOrNull() ?: "",
            pairingPortError = pairingPort.exceptionOrNull()?.message,
            adbPort     = adbPort.getOrElse { "5555" }
        )
    }
}