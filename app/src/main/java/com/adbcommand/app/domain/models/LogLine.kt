package com.adbcommand.app.domain.models

data class LogLine(
    val raw: String,
    val timestamp: String = "",
    val level: LogLevel = LogLevel.VERBOSE,
    val tag: String = "",
    val message: String = ""
)
