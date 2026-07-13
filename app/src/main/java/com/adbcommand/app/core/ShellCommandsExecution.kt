package com.adbcommand.app.core

import android.util.Log
import com.adbcommand.app.domain.models.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ShellCommandsExecution {
    suspend fun run(cmd: String): ShellResult = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val outputDeferred = async { process.inputStream.bufferedReader().readText() }
            val errorDeferred = async { process.errorStream.bufferedReader().readText() }

            val output = outputDeferred.await()
            val error = errorDeferred.await()
            val exitCode = process.waitFor()

            Log.d("ShellExecutor", "cmd=$cmd | exit=$exitCode | error=$error")

            ShellResult(
                output = output.trim(),
                error = error.trim(),
                success = exitCode == 0
            )
        } catch (e: Exception) {
            Log.e("ShellExecutor", "Failed to run command: $cmd", e)
            ShellResult(
                output = "",
                error = e.message ?: "Unknown error",
                success = false
            )
        } finally {
            process?.destroy()
        }
    }
}