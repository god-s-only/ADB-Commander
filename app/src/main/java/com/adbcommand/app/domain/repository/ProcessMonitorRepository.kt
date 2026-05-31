package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.ProcessInfo
import kotlinx.coroutines.flow.Flow

interface ProcessMonitorRepository {
    fun streamProcesses(intervalMs: Long = 1500): Flow<List<ProcessInfo>>
}