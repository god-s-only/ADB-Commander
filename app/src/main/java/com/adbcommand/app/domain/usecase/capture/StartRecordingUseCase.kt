package com.adbcommand.app.domain.usecase.capture

import com.adbcommand.app.domain.repository.CaptureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartRecordingUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    operator fun invoke(): Flow<Long> =
        repository.startRecording()
}