package com.adbcommand.app.domain.models

import android.graphics.Bitmap

data class CapturedScreenshot(
    val bitmap: Bitmap,
    val filePath: String,
    val capturedAtMs: Long = System.currentTimeMillis(),
    val sizeBytes: Long    = 0L
)