package com.adbcommand.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShellCommandsExecution {
    suspend fun run(cmd: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))

            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            ShellResult(
                output = output.trim(),
                error = error.trim(),
                success = exitCode == 0
            )
        } catch (e: Exception) {
            ShellResult(
                output = "",
                error = e.message ?: "Unknown error",
                success = false
            )
        }
    }
}
data class ShellResult(
    val output: String,
    val error: String,
    val success: Boolean
)