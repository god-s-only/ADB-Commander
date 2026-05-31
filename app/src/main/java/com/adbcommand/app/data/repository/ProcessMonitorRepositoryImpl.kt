package com.adbcommand.app.data.repository

import android.util.Log
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.models.ProcessInfo
import com.adbcommand.app.domain.repository.ProcessMonitorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class ProcessMonitorRepositoryImpl @Inject constructor(
    private val shizuku: ShizukuManager
) : ProcessMonitorRepository {

    companion object {
        private const val TAG = "ProcessMonitorRepo"
    }

    override fun streamProcesses(intervalMs: Long): Flow<List<ProcessInfo>> = flow {
        var prevTicks = mapOf<Int, Long>()
        var prevTotal = systemTotalTicks()

        while (true) {
            try {
                val pids     = readPids()
                val currTotal = systemTotalTicks()
                val totalDiff = (currTotal - prevTotal).coerceAtLeast(1L)

                val processes = pids.mapNotNull { pid ->
                    runCatching { buildProcessInfo(pid, prevTicks, totalDiff) }.getOrNull()
                }.sortedByDescending { it.cpuPercent }

                prevTicks = pids.associateWith { pid ->
                    runCatching { pidTicks(pid) }.getOrDefault(0L)
                }
                prevTotal = currTotal

                emit(processes)
            } catch (e: Exception) {
                Log.e(TAG, "streamProcesses error", e)
            }

            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun readPids(): List<Int> {
        val result = shizuku.run("ls /proc | grep -E '^[0-9]+$'")
        return result.output.lines()
            .mapNotNull { it.trim().toIntOrNull() }
    }

    private suspend fun buildProcessInfo(
        pid: Int,
        prevTicks: Map<Int, Long>,
        totalDiff: Long
    ): ProcessInfo {
        val stat = shizuku.run("cat /proc/$pid/stat").output
        val name = stat.substringAfter("(").substringBefore(")").trim()

        val fields = stat.substringAfterLast(")").trim().split(" ")
        val utime = fields.getOrNull(11)?.toLongOrNull() ?: 0L
        val stime = fields.getOrNull(12)?.toLongOrNull() ?: 0L
        val currTicks = utime + stime
        val prevTick = prevTicks[pid] ?: currTicks
        val tickDiff = (currTicks - prevTick).coerceAtLeast(0L)
        val cpuPercent = (tickDiff.toFloat() / totalDiff.toFloat() * 100f)
            .coerceIn(0f, 100f)

        val status = shizuku.run("grep VmRSS /proc/$pid/status").output
        val ramKb  = status.filter { it.isDigit() || it == ' ' }
            .trim().split(" ").firstOrNull()?.toLongOrNull() ?: 0L

        val uidLine = shizuku.run("grep ^Uid /proc/$pid/status").output
        val uid = uidLine.trim().split("\\s+".toRegex()).getOrNull(1)?.toIntOrNull() ?: 0
        val isUserApp = uid >= 10000

        return ProcessInfo(
            pid = pid,
            name = name,
            cpuPercent = cpuPercent,
            ramKb = ramKb,
            isUserApp = isUserApp
        )
    }

    private suspend fun systemTotalTicks(): Long {
        val line = shizuku.run("head -1 /proc/stat").output
        return line.split("\\s+".toRegex())
            .drop(1)
            .mapNotNull { it.toLongOrNull() }
            .sum()
            .coerceAtLeast(1L)
    }

    private suspend fun pidTicks(pid: Int): Long {
        val stat = shizuku.run("cat /proc/$pid/stat").output
        val fields = stat.substringAfterLast(")").trim().split(" ")
        val utime = fields.getOrNull(11)?.toLongOrNull() ?: 0L
        val stime = fields.getOrNull(12)?.toLongOrNull() ?: 0L
        return utime + stime
    }
}