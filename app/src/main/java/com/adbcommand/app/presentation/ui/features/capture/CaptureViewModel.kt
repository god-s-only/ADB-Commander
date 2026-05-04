package com.adbcommand.app.presentation.ui.features.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.CaptureCommands.takeScreenshot
import com.adbcommand.app.domain.models.RecordingState
import com.adbcommand.app.domain.usecase.capture.SaveScreenshotToGalleryUseCase
import com.adbcommand.app.domain.usecase.capture.ShareRecordingUseCase
import com.adbcommand.app.domain.usecase.capture.ShareScreenshotUseCase
import com.adbcommand.app.domain.usecase.capture.StartRecordingUseCase
import com.adbcommand.app.domain.usecase.capture.StopRecordingUseCase
import com.adbcommand.app.domain.usecase.capture.TakeScreenshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val takeScreenshot: TakeScreenshotUseCase,
    private val saveToGallery: SaveScreenshotToGalleryUseCase,
    private val shareScreenshot: ShareScreenshotUseCase,
    private val startRecording: StartRecordingUseCase,
    private val stopRecording: StopRecordingUseCase,
    private val shareRecording: ShareRecordingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null

    fun onEvent(event: CaptureEvent) {
        when (event) {
            is CaptureEvent.TakeScreenshot -> captureScreenshot()
            is CaptureEvent.SaveScreenshot -> saveScreenshot()
            is CaptureEvent.ShareScreenshot -> shareCurrentScreenshot()
            is CaptureEvent.DismissScreenshot ->
                _uiState.update { it.copy(screenshot = null, screenshotSaved = false) }
            is CaptureEvent.StartRecording -> beginRecording()
            is CaptureEvent.StopRecording -> endRecording()
            is CaptureEvent.ShareRecording -> shareCurrentRecording(event.path)
            is CaptureEvent.DismissMessage ->
                _uiState.update { it.copy(message = null) }
            is CaptureEvent.DismissError      ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun captureScreenshot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTakingScreenshot = true, error = null) }

            takeScreenshot().fold(
                onSuccess = { shot ->
                    _uiState.update {
                        it.copy(
                            isTakingScreenshot = false,
                            screenshot= shot,
                            screenshotSaved = false
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isTakingScreenshot = false,
                            error = "Screenshot failed: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    private fun saveScreenshot() {
        val path = _uiState.value.screenshot?.filePath ?: return
        viewModelScope.launch {
            saveToGallery(path).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(screenshotSaved = true, message = "Saved to gallery")
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = "Save failed: ${err.message}") }
                }
            )
        }
    }

    private fun shareCurrentScreenshot() {
        val path = _uiState.value.screenshot?.filePath ?: return
        viewModelScope.launch {
            shareScreenshot(path)
        }
    }

    private fun beginRecording() {
        if (recordingJob?.isActive == true) return

        _uiState.update {
            it.copy(
                recordingState = RecordingState.Recording(System.currentTimeMillis()),
                recordingElapsedMs = 0L,
                error = null
            )
        }

        recordingJob = viewModelScope.launch {
            try {
                startRecording().collect { elapsedMs ->
                    _uiState.update { it.copy(recordingElapsedMs = elapsedMs) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        recordingState = RecordingState.Error(
                            e.message ?: "Recording failed"
                        ),
                        error = e.message
                    )
                }
            }
        }
    }

    private fun endRecording() {
        recordingJob?.cancel()
        recordingJob = null

        viewModelScope.launch {
            stopRecording().fold(
                onSuccess = { session ->
                    _uiState.update {
                        it.copy(
                            recordingState = RecordingState.Stopped(session),
                            lastRecording  = session,
                            message        = "Recording saved (${formatDuration(session.durationMs)})"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            recordingState = RecordingState.Error(err.message ?: "Stop failed"),
                            error          = "Stop failed: ${err.message}"
                        )
                    }
                }
            )
        }
    }

    private fun shareCurrentRecording(path: String) {
        viewModelScope.launch { shareRecording(path) }
    }

    override fun onCleared() {
        super.onCleared()
        if (recordingJob?.isActive == true) {
            recordingJob?.cancel()
            viewModelScope.launch { stopRecording() }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}