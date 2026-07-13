package com.adbcommand.app.domain.usecase.appmanager

import com.adbcommand.app.data.repository.ShizukuAppManagerRepository
import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.repository.AppManagerRepository
import jakarta.inject.Inject

class KillAppUseCase @Inject constructor(
    private val repository: ShizukuAppManagerRepository
) {
    suspend operator fun invoke(packageName: String): AppActionResult =
        repository.killApp(packageName)
}
