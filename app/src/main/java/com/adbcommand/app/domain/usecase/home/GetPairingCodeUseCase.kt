package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.domain.repository.HomeRepository
import jakarta.inject.Inject

class GetPairingCodeUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): Result<String> =
        repository.generatePairingCode()
}