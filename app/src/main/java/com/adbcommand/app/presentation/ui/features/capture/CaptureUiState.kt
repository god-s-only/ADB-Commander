package com.adbcommand.app.presentation.ui.features.capture

import com.adbcommand.app.domain.models.CapturedScreenshot
import com.adbcommand.app.domain.models.RecordingSession
import com.adbcommand.app.domain.models.RecordingState

data class CaptureUiState(
    val isTakingScreenshot: Boolean = false,
    val screenshot: CapturedScreenshot? = null,
    val screenshotSaved: Boolean = false,
    val recordingState: RecordingState = RecordingState.Idle,
    val recordingElapsedMs: Long = 0L,
    val lastRecording: RecordingSession? = null,
    val message: String? = null,
    val error: String? = null
)