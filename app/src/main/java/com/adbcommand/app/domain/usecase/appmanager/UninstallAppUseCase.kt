package com.adbcommand.app.domain.usecase.appmanager

import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.repository.AppManagerRepository
import jakarta.inject.Inject

class UninstallAppUseCase @Inject constructor(
    private val repository: AppManagerRepository
) {
    suspend operator fun invoke(packageName: String): AppActionResult =
        repository.uninstall(packageName)
}