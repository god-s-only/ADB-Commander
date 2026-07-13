package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.domain.repository.HomeRepository
import javax.inject.Inject

class GetDeviceIpUseCase @Inject constructor(private val repository: HomeRepository) {
    suspend operator fun invoke(): Result<String>{
        return repository.getDeviceIp()
    }
}