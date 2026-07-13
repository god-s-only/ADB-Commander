package com.adbcommand.app.domain.models

data class AdbCommand(
    val id: String,
    val category: CommandCategory,
    val title: String,
    val command: String,
    val hint: String = "",
    val needsInput: Boolean = false
)

enum class CommandCategory(val label: String) {
    CONNECTION("Connection"),
    APP_MANAGEMENT("App Management"),
    DEVICE_SYSTEM("Device & System"),
    CAPTURE("Capture"),
    LOGS("Logs")
}
