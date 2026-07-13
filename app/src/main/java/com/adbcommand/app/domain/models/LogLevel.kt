package com.adbcommand.app.domain.models

enum class LogLevel(val label: String) {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    FATAL("F"),
    SILENT("S");

    companion object {
        fun fromChar(c: String): LogLevel = entries.firstOrNull {
            it.label.equals(c, ignoreCase = true)
        } ?: VERBOSE
    }
}