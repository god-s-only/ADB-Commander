package com.adbcommand.app.domain.usecase.appmanager

import com.adbcommand.app.data.repository.ShizukuAppManagerRepository
import com.adbcommand.app.domain.models.AppInfo
import com.adbcommand.app.domain.repository.AppManagerRepository
import jakarta.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val repository: ShizukuAppManagerRepository
) {
    suspend operator fun invoke(includeSystem: Boolean = false): Result<List<AppInfo>> =
        repository.getInstalledApps(includeSystem)
}