package com.adbcommand.app.domain.usecase.capture

import com.adbcommand.app.domain.repository.CaptureRepository
import javax.inject.Inject

class ShareRecordingUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    suspend operator fun invoke(filePath: String): Result<Unit> =
        repository.shareRecording(filePath)
}