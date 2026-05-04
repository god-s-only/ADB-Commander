package com.adbcommand.app.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CaptureCommands {

    const val CAPTURE_DIR = "/sdcard/Pictures/AdbCommander"

    fun mkdirCapture(): String = "mkdir -p $CAPTURE_DIR"

    fun takeScreenshot(filePath: String): String =
        "screencap -p $filePath"

    fun newScreenshotPath(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "$CAPTURE_DIR/screenshot_$timestamp.png"
    }

    fun startRecording(filePath: String, bitrateMbps: Int = 8): String =
        "screenrecord --bit-rate ${bitrateMbps}000000 --time-limit 180 $filePath"

    fun newRecordingPath(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "$CAPTURE_DIR/recording_$timestamp.mp4"
    }

    fun getFileSize(filePath: String): String =
        "stat -c %s $filePath"
}