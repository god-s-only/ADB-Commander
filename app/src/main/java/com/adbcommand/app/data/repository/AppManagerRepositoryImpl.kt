package com.adbcommand.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo
import com.adbcommand.app.domain.repository.AppManagerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shell: ShellCommandsExecution
) : AppManagerRepository {

    companion object {
        private const val TAG = "AppManagerRepository"
    }

    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun getInstalledApps(
        includeSystem: Boolean
    ): Result<List<AppInfo>> = withContext(Dispatchers.IO) {
        try {
            val pm  = context.packageManager
            val flags = PackageManager.GET_META_DATA

            @Suppress("DEPRECATION")
            val packages = pm.getInstalledApplications(flags)

            val apps = packages
                .filter { appInfo ->
                    includeSystem || (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
                }
                .mapNotNull { appInfo ->
                    try {
                        val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo.packageName),
                            versionName = packageInfo.versionName ?: "—",
                            isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Skipping ${appInfo.packageName}: ${e.message}")
                        null
                    }
                }
                .sortedBy { it.appName.lowercase() }

            Result.success(apps)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load installed apps", e)
            Result.failure(e)
        }
    }

    override suspend fun killApp(packageName: String): AppActionResult {
        val result = shell.run(Commands.killApp(packageName))
        return if (result.success || result.error.isBlank()) {
            AppActionResult.Success("$packageName stopped")
        } else {
            AppActionResult.Failure(result.error)
        }
    }


    override suspend fun clearData(packageName: String): AppActionResult {
        val result = shell.run(Commands.clearData(packageName))
        return if (result.success && result.output.contains("Success", ignoreCase = true)) {
            AppActionResult.Success("Data cleared for $packageName")
        } else {
            AppActionResult.Failure(
                result.error.ifBlank { result.output.ifBlank { "Clear data failed" } }
            )
        }
    }

    override suspend fun extractApk(packageName: String): AppActionResult {
        val pathResult = shell.run(Commands.getApkPath(packageName))
        if (!pathResult.success || pathResult.output.isBlank()) {
            return AppActionResult.Failure("Could not find APK for $packageName")
        }

        val apkPath = pathResult.output
            .lineSequence()
            .firstOrNull { it.startsWith("package:") }
            ?.removePrefix("package:")
            ?.trim()
            ?: return AppActionResult.Failure("Could not parse APK path")

        val copyResult = shell.run(Commands.copyApkToDownloads(apkPath, packageName))
        return if (copyResult.success) {
            AppActionResult.Success(
                "APK saved to Downloads/${packageName}.apk"
            )
        } else {
            AppActionResult.Failure(
                copyResult.error.ifBlank { "Failed to copy APK" }
            )
        }
    }

    override suspend fun uninstall(packageName: String): AppActionResult {
        val result = shell.run(Commands.uninstall(packageName))
        return if (result.success && result.output.contains("Success", ignoreCase = true)) {
            AppActionResult.Success("$packageName uninstalled")
        } else {
            AppActionResult.Failure(
                result.error.ifBlank { result.output.ifBlank { "Uninstall failed" } }
            )
        }
    }
    override suspend fun launchApp(packageName: String): AppActionResult {
        val result = shell.run(Commands.launchApp(packageName))
        return if (result.success) {
            AppActionResult.Success("Launched $packageName")
        } else {
            AppActionResult.Failure(
                result.error.ifBlank { "Failed to launch $packageName" }
            )
        }
    }
}