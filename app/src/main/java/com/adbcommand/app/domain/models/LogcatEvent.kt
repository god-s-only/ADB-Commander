package com.adbcommand.app.domain.models

sealed class LogcatEvent {
    data class Line(val logLine: LogLine) : LogcatEvent()
    object Started: LogcatEvent()
    object Stopped: LogcatEvent()
    data class Error(val message: String): LogcatEvent()
    object ToggleAutoScroll: LogcatEvent()
    object DismissSaveResult: LogcatEvent()
    data class LevelChanged(val level: LogLevel): LogcatEvent()
    data class TagChanged(val tag: String): LogcatEvent()
    data class SearchChanged(val query: String): LogcatEvent()
    object Save: LogcatEvent()
    object Clear: LogcatEvent()

}