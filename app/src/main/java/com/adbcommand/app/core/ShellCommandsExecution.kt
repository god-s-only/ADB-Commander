package com.adbcommand.app.core

import com.adbcommand.app.domain.models.ShellResult
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