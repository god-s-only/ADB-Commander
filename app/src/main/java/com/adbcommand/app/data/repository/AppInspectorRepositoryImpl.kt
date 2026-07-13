package com.adbcommand.app.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.Build
import android.util.Log
import com.adbcommand.app.domain.models.AppComponent
import com.adbcommand.app.domain.models.AppInspection
import com.adbcommand.app.domain.repository.AppInspectorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class AppInspectorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppInspectorRepository {

    companion object {
        private const val TAG = "AppInspectorRepo"
    }

    @Suppress("DEPRECATION")
    override suspend fun inspectApp(packageName: String): Result<AppInspection> =
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    GET_PERMISSIONS or GET_ACTIVITIES or GET_SERVICES or
                            GET_RECEIVERS   or GET_PROVIDERS  or GET_SIGNING_CERTIFICATES
                } else {
                    GET_PERMISSIONS or GET_ACTIVITIES or GET_SERVICES or
                            GET_RECEIVERS   or GET_PROVIDERS  or GET_SIGNATURES
                }

                val pkgInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(packageName, PackageInfoFlags.of(flags.toLong()))
                } else {
                    pm.getPackageInfo(packageName, flags)
                }

                val appInfo = pkgInfo.applicationInfo

                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo.longVersionCode
                } else {
                    pkgInfo.versionCode.toLong()
                }

                val isDebuggable = appInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

                val isTestOnly = appInfo?.flags?.and(android.content.pm.ApplicationInfo.FLAG_TEST_ONLY) != 0

                val apkFile   = File(appInfo?.publicSourceDir)
                val apkSize   = if (apkFile.exists()) apkFile.length() else null

                val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    runCatching {
                        pm.getInstallSourceInfo(packageName).installingPackageName
                    }.getOrNull()
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(packageName)
                }

                var certSubject: String? = null
                var certSha256: String?  = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val signingInfo = pkgInfo.signingInfo
                    val cert = if (signingInfo?.hasMultipleSigners() ?: false) {
                        signingInfo?.apkContentsSigners?.firstOrNull()
                    } else {
                        signingInfo?.signingCertificateHistory?.firstOrNull()
                    }
                    cert?.let {
                        certSha256 = sha256Hex(it.toByteArray())
                        certSubject = parseCertSubject(it.toByteArray())
                    }
                } else {
                    val sig = pkgInfo.signatures?.firstOrNull()
                    sig?.let {
                        certSha256  = sha256Hex(it.toByteArray())
                        certSubject = parseCertSubject(it.toByteArray())
                    }
                }

                val activities = pkgInfo.activities?.map { ai ->
                    AppComponent(
                        name = ai.name,
                        shortName = ai.name.substringAfterLast("."),
                        isExported = ai.exported,
                        hasIntentFilter = ai.exported
                    )
                } ?: emptyList()

                val services = pkgInfo.services?.map { si ->
                    AppComponent(
                        name = si.name,
                        shortName = si.name.substringAfterLast("."),
                        isExported = si.exported,
                        hasIntentFilter = si.exported
                    )
                } ?: emptyList()

                val receivers = pkgInfo.receivers?.map { ri ->
                    AppComponent(
                        name = ri.name,
                        shortName = ri.name.substringAfterLast("."),
                        isExported = ri.exported,
                        hasIntentFilter = ri.exported
                    )
                } ?: emptyList()

                val providers = pkgInfo.providers?.map { pi ->
                    AppComponent(
                        name = pi.name,
                        shortName = pi.name.substringAfterLast("."),
                        isExported = pi.exported,
                        hasIntentFilter = false
                    )
                } ?: emptyList()

                val requestedPerms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
                val permFlags      = pkgInfo.requestedPermissionsFlags ?: IntArray(0)

                val granted = mutableListOf<String>()
                val denied  = mutableListOf<String>()

                requestedPerms.forEachIndexed { index, perm ->
                    val flag = if (index < permFlags.size) permFlags[index] else 0
                    if (flag and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                        granted.add(perm)
                    } else {
                        denied.add(perm)
                    }
                }

                val targetSdk  = appInfo?.targetSdkVersion.takeIf { it!! > 0 }
                val minSdk     = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfo?.minSdkVersion.takeIf { it!! > 0 }
                } else null
                val compileSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    appInfo?.compileSdkVersion.takeIf { it!! > 0 }
                } else null

                Result.success(
                    AppInspection(
                        packageName         = packageName,
                        appName             = pm.getApplicationLabel(appInfo!!).toString(),
                        icon                = runCatching { pm.getApplicationIcon(packageName) }.getOrNull(),
                        versionName         = pkgInfo.versionName,
                        versionCode         = versionCode,
                        installedAt         = pkgInfo.firstInstallTime,
                        updatedAt           = pkgInfo.lastUpdateTime,
                        installerPackage    = installer,
                        targetSdk           = targetSdk,
                        minSdk              = minSdk,
                        compileSdk          = compileSdk,
                        isDebuggable        = isDebuggable,
                        isTestOnly          = isTestOnly,
                        apkPath             = appInfo.publicSourceDir,
                        apkSizeBytes        = apkSize,
                        dataDir             = appInfo.dataDir,
                        signingCertSubject  = certSubject,
                        signingCertSha256   = certSha256,
                        activities          = activities,
                        services            = services,
                        receivers           = receivers,
                        providers           = providers,
                        grantedPermissions  = granted.sorted(),
                        deniedPermissions   = denied.sorted(),
                        requestedPermissions = requestedPerms.sorted()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "inspectApp failed for $packageName", e)
                Result.failure(e)
            }
        }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(":") { "%02X".format(it) }
    }

    private fun parseCertSubject(der: ByteArray): String? {
        return try {
            val cert = java.security.cert.CertificateFactory
                .getInstance("X.509")
                .generateCertificate(der.inputStream())
                    as java.security.cert.X509Certificate
            cert.subjectDN.name
        } catch (e: Exception) {
            null
        }
    }
}