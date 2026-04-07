package com.adbcommand.app.domain.usecase.logcat

import com.adbcommand.app.domain.models.LogcatEvent
import com.adbcommand.app.domain.models.LogcatFilter
import com.adbcommand.app.domain.repository.LogcatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StreamLogcatUseCase @Inject constructor(
    private val repository: LogcatRepository
) {
    operator fun invoke(filter: LogcatFilter): Flow<LogcatEvent> =
        repository.streamLogcat(filter)
}