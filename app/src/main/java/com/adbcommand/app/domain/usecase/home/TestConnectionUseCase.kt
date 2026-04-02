package com.adbcommand.app.domain.usecase.home

import com.adbcommand.app.domain.repository.HomeRepository
import javax.inject.Inject

class TestConnectionUseCase @Inject constructor(private val repository: HomeRepository) {
    suspend operator fun invoke(): Result<String>{
        return repository.testConnection()
    }
}