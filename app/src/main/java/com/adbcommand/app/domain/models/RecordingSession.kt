package com.adbcommand.app.domain.models

data class RecordingSession(
    val filePath: String,
    val startedAtMs: Long = System.currentTimeMillis(),
    val isComplete: Boolean = false,
    val durationMs: Long = 0L
)