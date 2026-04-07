package com.adbcommand.app.presentation.ui.features.logcat

import com.adbcommand.app.domain.models.LogLine
import com.adbcommand.app.domain.models.LogcatFilter

data class LogcatUiState(
    val lines: List<LogLine> = emptyList(),
    val isRunning: Boolean = false,
    val filter: LogcatFilter = LogcatFilter(),
    val autoScroll: Boolean = true,
    val isSaving: Boolean = false,
    val saveResult: String? = null,
    val error: String? = null
)