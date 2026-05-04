package com.adbcommand.app.domain.models

sealed class RecordingState {
    object Idle: RecordingState()
    data class Recording(val startedAtMs: Long)  : RecordingState()
    data class Stopped(val session: RecordingSession) : RecordingState()
    data class Error(val message: String) : RecordingState()
}