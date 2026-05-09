package com.adbcommand.app.domain.models

import android.graphics.drawable.Drawable

data class AppSummary(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val versionName: String,
    val isSystem: Boolean,
    val targetSdk: Int?,
    val isDebuggable: Boolean
)