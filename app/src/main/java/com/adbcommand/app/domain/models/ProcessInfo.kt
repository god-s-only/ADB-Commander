package com.adbcommand.app.domain.models

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Float,
    val ramKb: Long,
    val isUserApp: Boolean
) {
    val ramMb: Float get() = ramKb / 1024f
}