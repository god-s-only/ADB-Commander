package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo

interface AppManagerRepository {
    suspend fun getInstalledApps(includeSystem: Boolean = false): Result<List<AppInfo>>

    suspend fun killApp(packageName: String): AppActionResult

    suspend fun clearData(packageName: String): AppActionResult

    suspend fun extractApk(packageName: String): AppActionResult

    suspend fun uninstall(packageName: String): AppActionResult

    suspend fun launchApp(packageName: String): AppActionResult
}