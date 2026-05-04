package com.adbcommand.app.presentation.ui.features.capture

sealed class CaptureEvent {
    object TakeScreenshot : CaptureEvent()
    object SaveScreenshot : CaptureEvent()
    object ShareScreenshot : CaptureEvent()
    object DismissScreenshot: CaptureEvent()
    object StartRecording : CaptureEvent()
    object StopRecording: CaptureEvent()
    data class ShareRecording(val path: String): CaptureEvent()
    object DismissMessage: CaptureEvent()
    object DismissError: CaptureEvent()
}