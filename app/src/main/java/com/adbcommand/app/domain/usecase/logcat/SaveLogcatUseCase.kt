package com.adbcommand.app.domain.usecase.logcat

import com.adbcommand.app.domain.repository.LogcatRepository
import javax.inject.Inject

class SaveLogcatUseCase @Inject constructor(
    private val repository: LogcatRepository
) {
    suspend operator fun invoke(lines: List<String>): Result<String> =
        repository.saveToFile(lines)
}