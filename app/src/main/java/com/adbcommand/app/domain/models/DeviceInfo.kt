package com.adbcommand.app.domain.models

data class DeviceInfo(
    val ip: String = "",
    val ipError: String? = null,
    val pairingPort: String = "",
    val pairingPortError: String? = null,
    val adbPort: String = "",

    val model: String? = null,
    val manufacturer: String? = null,
    val androidVersion: String? = null,
    val apiLevel: String? = null,
    val buildNumber: String? = null,
    val securityPatch: String? = null,

    val screenSize: String? = null,
    val screenDensity: String? = null,
    val cpuAbi: String? = null,
    val totalRam: String? = null,

    val batteryLevel: String? = null,
    val batteryStatus: String? = null,
    val batteryHealth: String? = null,
    val batteryTemp: String? = null,
    val batteryVoltage: String? = null,

    val ipAddress: String? = null,
    val wifiState: String? = null,
){
    fun toProfileString(): String = buildString {
        appendLine("=== ADB Commander — Device Profile ===")
        appendLine()
        appendLine("── System ──────────────────────────")
        appendLine("Model          : ${model ?: "Unknown"}")
        appendLine("Manufacturer   : ${manufacturer ?: "Unknown"}")
        appendLine("Android        : ${androidVersion ?: "Unknown"}")
        appendLine("API Level      : ${apiLevel ?: "Unknown"}")
        appendLine("Build          : ${buildNumber ?: "Unknown"}")
        appendLine("Security Patch : ${securityPatch ?: "Unknown"}")
        appendLine()
        appendLine("── Hardware ─────────────────────────")
        appendLine("Screen Size    : ${screenSize ?: "Unknown"}")
        appendLine("Screen Density : ${screenDensity ?: "Unknown"}")
        appendLine("CPU ABI        : ${cpuAbi ?: "Unknown"}")
        appendLine("Total RAM      : ${totalRam ?: "Unknown"}")
        appendLine()
        appendLine("── Battery ──────────────────────────")
        appendLine("Level          : ${batteryLevel?.let { "$it%" } ?: "Unknown"}")
        appendLine("Status         : ${batteryStatus ?: "Unknown"}")
        appendLine("Health         : ${batteryHealth ?: "Unknown"}")
        appendLine("Temperature    : ${batteryTemp ?: "Unknown"}")
        appendLine("Voltage        : ${batteryVoltage ?: "Unknown"}")
        appendLine()
        appendLine("── Network ──────────────────────────")
        appendLine("IP Address     : ${ipAddress ?: "Unknown"}")
        appendLine("Wi-Fi          : ${wifiState ?: "Unknown"}")
    }
}