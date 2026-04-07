package com.adbcommand.app.domain.models

sealed class LogcatEvent {
    data class Line(val logLine: LogLine) : LogcatEvent()
    object Started: LogcatEvent()
    object Stopped: LogcatEvent()
    data class Error(val message: String): LogcatEvent()
}