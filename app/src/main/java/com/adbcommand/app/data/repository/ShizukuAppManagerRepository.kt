package com.adbcommand.app.data.repository

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import com.adbcommand.app.core.Commands
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo
import com.adbcommand.app.domain.repository.AppManagerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShizukuAppManagerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizuku: ShizukuManager
) : AppManagerRepository {

    companion object {
        private const val TAG = "ShizukuAppManagerRepo"
    }

    override suspend fun getInstalledApps(
        includeSystem: Boolean
    ): Result<List<AppInfo>> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager

            @Suppress("DEPRECATION")
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            val apps = packages
                .filter { appInfo ->
                    if (includeSystem) true
                    else {
                        val isSystem  = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        val isUpdatedSystem = appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                        !isSystem || isUpdatedSystem
                    }
                }
                .mapNotNull { appInfo ->
                    try {
                        val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = runCatching {
                                pm.getApplicationIcon(appInfo.packageName)
                            }.getOrNull(),
                            versionName = packageInfo.versionName ?: "—",
                            isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.appName.lowercase() }

            Result.success(apps)
        } catch (e: Exception) {
            Log.e(TAG, "getInstalledApps failed", e)
            Result.failure(e)
        }
    }

    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    override suspend fun killApp(packageName: String): AppActionResult {
        return if (shizuku.isAvailable()) {
            val result = shizuku.run(Commands.killApp(packageName))
            if (result.success || result.error.isBlank()) {
                AppActionResult.Success("Force stopped $packageName")
            } else {
                AppActionResult.Failure(result.error)
            }
        } else {
            fallbackKill(packageName)
        }
    }

    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    private fun fallbackKill(packageName: String): AppActionResult {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses(packageName)
            AppActionResult.Success("Background processes stopped for $packageName\n(Install Shizuku to force-stop foreground apps)")
        } catch (e: Exception) {
            AppActionResult.Failure("Kill failed: ${e.message}")
        }
    }


    override suspend fun clearData(packageName: String): AppActionResult {
        return if (shizuku.isAvailable()) {
            val result = shizuku.run(Commands.clearData(packageName))
            if (result.success && result.output.contains("Success", ignoreCase = true)) {
                AppActionResult.Success("Data cleared for $packageName")
            } else {
                AppActionResult.Failure(
                    result.error.ifBlank { result.output.ifBlank { "pm clear failed" } }
                )
            }
        } else {
            fallbackClearData(packageName)
        }
    }

    private fun fallbackClearData(packageName: String): AppActionResult {
        return try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data  = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppActionResult.Success("Opened App Info — tap 'Clear Data'\n(Install Shizuku to clear data instantly)")
        } catch (e: Exception) {
            AppActionResult.Failure("Could not open App Info: ${e.message}")
        }
    }

    override suspend fun extractApk(packageName: String): AppActionResult =
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val source  = java.io.File(appInfo.publicSourceDir)

                if (!source.exists()) {
                    return@withContext AppActionResult.Failure("APK not found at ${appInfo.publicSourceDir}")
                }
                val destDir = java.io.File(
                    android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    ),
                    "ExtractedAPKs"
                )
                destDir.mkdirs()
                val dest = java.io.File(destDir, "$packageName.apk")
                source.copyTo(dest, overwrite = true)

                AppActionResult.Success("APK saved to Downloads/ExtractedAPKs/$packageName.apk")
            } catch (e: Exception) {
                Log.e(TAG, "extractApk failed for $packageName", e)
                AppActionResult.Failure("Extract failed: ${e.message}")
            }
        }


    override suspend fun uninstall(packageName: String): AppActionResult {
        return if (shizuku.isAvailable()) {
            val result = shizuku.run(Commands.uninstall(packageName))
            if (result.success && result.output.contains("Success", ignoreCase = true)) {
                AppActionResult.Success("$packageName uninstalled")
            } else {
                AppActionResult.Failure(
                    result.error.ifBlank { result.output.ifBlank { "Uninstall failed" } }
                )
            }
        } else {
            fallbackUninstall(packageName)
        }
    }

    private fun fallbackUninstall(packageName: String): AppActionResult {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data  = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AppActionResult.Success("System uninstall dialog opened\n(Install Shizuku to uninstall silently)")
        } catch (e: Exception) {
            AppActionResult.Failure("Could not open uninstall dialog: ${e.message}")
        }
    }

    override suspend fun launchApp(packageName: String): AppActionResult {
        return try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(packageName)
                ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                ?: return AppActionResult.Failure("No launcher activity found for $packageName")

            context.startActivity(intent)
            AppActionResult.Success("Launched $packageName")
        } catch (e: Exception) {
            AppActionResult.Failure("Launch failed: ${e.message}")
        }
    }
}