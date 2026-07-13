package com.adbcommand.app.domain.models

import android.graphics.drawable.Drawable

data class AppInspection(

    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val versionName: String?,
    val versionCode: Long?,
    val installedAt: Long?,
    val updatedAt: Long?,
    val installerPackage: String?,

    val targetSdk: Int?,
    val minSdk: Int?,
    val compileSdk: Int?,
    val isDebuggable: Boolean,
    val isTestOnly: Boolean,

    val apkPath: String?,
    val apkSizeBytes: Long?,
    val dataDir: String?,

    val signingCertSubject: String?,
    val signingCertSha256: String?,

    val activities: List<AppComponent>,
    val services: List<AppComponent>,
    val receivers: List<AppComponent>,
    val providers: List<AppComponent>,

    val grantedPermissions: List<String>,
    val deniedPermissions: List<String>,
    val requestedPermissions: List<String>
)