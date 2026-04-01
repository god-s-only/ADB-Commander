package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.core.PairingResult
import com.adbcommand.app.domain.models.PairingCredentials
import com.adbcommand.app.domain.repository.PairingRepository
import jakarta.inject.Inject

class PairAdbDeviceUseCase @Inject constructor(
    private val repository: PairingRepository
) {
    suspend operator fun invoke(credentials: PairingCredentials): PairingResult =
        repository.pairDevice(credentials)
}