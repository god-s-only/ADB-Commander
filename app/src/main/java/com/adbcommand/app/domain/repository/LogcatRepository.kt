package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.LogcatEvent
import com.adbcommand.app.domain.models.LogcatFilter
import kotlinx.coroutines.flow.Flow

interface LogcatRepository {

    fun streamLogcat(filter: LogcatFilter): Flow<LogcatEvent>

    suspend fun saveToFile(lines: List<String>): Result<String>
}