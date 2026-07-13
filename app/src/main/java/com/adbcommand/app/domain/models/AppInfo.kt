package com.adbcommand.app.domain.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val versionName: String,
    val isSystemApp: Boolean
)

sealed class AppActionResult {
    data class Success(val message: String) : AppActionResult()
    data class Failure(val message: String) : AppActionResult()
}