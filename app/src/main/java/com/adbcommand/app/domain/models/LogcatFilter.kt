package com.adbcommand.app.domain.models

data class LogcatFilter(
    val level: LogLevel   = LogLevel.VERBOSE,
    val tag: String = "",
    val searchQuery: String = ""
)
