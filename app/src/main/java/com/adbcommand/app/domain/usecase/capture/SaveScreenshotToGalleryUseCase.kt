package com.adbcommand.app.domain.usecase.capture

import com.adbcommand.app.domain.repository.CaptureRepository
import javax.inject.Inject

class SaveScreenshotToGalleryUseCase @Inject constructor(
    private val repository: CaptureRepository
) {
    suspend operator fun invoke(filePath: String): Result<String> =
        repository.saveToGallery(filePath)
}