package com.adbcommand.app.domain.usecase.capture

import com.adbcommand.app.domain.models.RecordingSession
import com.adbcommand.app.domain.repository.CaptureRepository
import javax.inject.Inject

class StopRecordingUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    suspend operator fun invoke(): Result<RecordingSession> =
        repository.stopRecording()
}