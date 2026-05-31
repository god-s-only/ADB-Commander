package com.adbcommand.app.domain.usecase.process

import com.adbcommand.app.domain.repository.ProcessMonitorRepository
import javax.inject.Inject

class GetStreamProcessesUseCase @Inject constructor(private val processMonitorRepository: ProcessMonitorRepository) {
    operator fun invoke() = processMonitorRepository.streamProcesses()
}