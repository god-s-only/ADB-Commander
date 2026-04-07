package com.adbcommand.app.data.repository

import android.os.Environment
import android.util.Log
import com.adbcommand.app.core.LogcatParser
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.models.LogcatEvent
import com.adbcommand.app.domain.models.LogcatFilter
import com.adbcommand.app.domain.models.LogLevel
import com.adbcommand.app.domain.repository.LogcatRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class LogcatRepositoryImpl @Inject constructor(
    private val shizuku: ShizukuManager
) : LogcatRepository {

    companion object {
        private const val TAG = "LogcatRepository"
        private const val MAX_LINES = 2000
    }


    override fun streamLogcat(filter: LogcatFilter): Flow<LogcatEvent> = flow {

        var process: Process? = null

        try {
            val levelFlag = filter.level.label
            val cmd = buildString {
                append("logcat -v threadtime *:$levelFlag")
                if (filter.tag.isNotBlank()) {
                    append(" -s ${filter.tag}:$levelFlag")
                }
            }

            Log.d(TAG, "Starting logcat: $cmd")
            process = if (shizuku.isAvailable()) {
                startViaShizuku(cmd)
            } else {
                startViaRuntime(cmd)
            }
            if (process == null) {
                emit(LogcatEvent.Error("Failed to start logcat process"))
                return@flow
            }

            emit(LogcatEvent.Started)

            val reader = BufferedReader(InputStreamReader(process.inputStream))

            while (currentCoroutineContext().isActive) {
                val line = reader.readLine() ?: break

                val logLine = LogcatParser.parse(line)

                if (filter.searchQuery.isNotBlank() &&
                    !line.contains(filter.searchQuery, ignoreCase = true)) {
                    continue
                }

                emit(LogcatEvent.Line(logLine))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Logcat stream error", e)
            emit(LogcatEvent.Error(e.message ?: "Unknown error"))
        } finally {
            process?.destroy()
            emit(LogcatEvent.Stopped)
        }

    }.flowOn(Dispatchers.IO)

    private fun startViaShizuku(cmd: String): Process? {
        return try {
            val clazz  = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, arrayOf("sh", "-c", cmd), null, null) as? Process
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku newProcess failed, falling back to Runtime", e)
            startViaRuntime(cmd)
        }
    }

    private fun startViaRuntime(cmd: String): Process? {
        return try {
            Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        } catch (e: Exception) {
            Log.e(TAG, "Runtime.exec failed for logcat", e)
            null
        }
    }


    override suspend fun saveToFile(lines: List<String>): Result<String> {
        return try {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ),
                "AdbCommander"
            )
            dir.mkdirs()
            val file = File(dir, "logcat_${System.currentTimeMillis()}.txt")
            file.writeText(lines.joinToString("\n"))

            Log.d(TAG, "Logcat saved to ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "saveToFile failed", e)
            Result.failure(e)
        }
    }
}