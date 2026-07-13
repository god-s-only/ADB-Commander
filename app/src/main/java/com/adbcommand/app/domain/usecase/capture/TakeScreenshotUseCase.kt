package com.adbcommand.app.domain.usecase.capture

import com.adbcommand.app.domain.models.CapturedScreenshot
import com.adbcommand.app.domain.repository.CaptureRepository
import javax.inject.Inject

class TakeScreenshotUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    suspend operator fun invoke(): Result<CapturedScreenshot> =
        repository.takeScreenshot()
}