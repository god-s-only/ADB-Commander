package com.adbcommand.app.domain.usecase.appmanager

import com.adbcommand.app.domain.models.AppInfo
import com.adbcommand.app.domain.repository.AppManagerRepository
import jakarta.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val repository: AppManagerRepository
) {
    suspend operator fun invoke(includeSystem: Boolean = false): Result<List<AppInfo>> =
        repository.getInstalledApps(includeSystem)
}