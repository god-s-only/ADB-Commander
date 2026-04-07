package com.adbcommand.app.presentation.ui.features.logcat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.models.LogcatEvent
import com.adbcommand.app.domain.models.LogcatFilter
import com.adbcommand.app.domain.usecase.logcat.SaveLogcatUseCase
import com.adbcommand.app.domain.usecase.logcat.StreamLogcatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val streamLogcat: StreamLogcatUseCase,
    private val saveLogcat: SaveLogcatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogcatUiState())
    val uiState: StateFlow<LogcatUiState> = _uiState.asStateFlow()
    private var logcatJob: Job? = null

    private val maxLines = 2000

    fun onEvent(event: LogcatEvent) {
        when (event) {
            is LogcatEvent.Started -> start()
            is LogcatEvent.Stopped -> stop()
            is LogcatEvent.Clear -> clear()
            is LogcatEvent.Save -> save()
            is LogcatEvent.ToggleAutoScroll ->
                _uiState.update { it.copy(autoScroll = !it.autoScroll) }
            is LogcatEvent.DismissSaveResult ->
                _uiState.update { it.copy(saveResult = null) }
            is LogcatEvent.LevelChanged -> restartWithFilter(
                _uiState.value.filter.copy(level = event.level)
            )
            is LogcatEvent.TagChanged     -> restartWithFilter(
                _uiState.value.filter.copy(tag = event.tag)
            )
            is LogcatEvent.SearchChanged  ->
                _uiState.update {
                    it.copy(filter = it.filter.copy(searchQuery = event.query))
                }

            else -> {}
        }
    }


    private fun start() {
        if (logcatJob?.isActive == true) return

        logcatJob = viewModelScope.launch {
            streamLogcat(_uiState.value.filter).collect { event ->
                when (event) {
                    is com.adbcommand.app.domain.models.LogcatEvent.Started -> {
                        _uiState.update { it.copy(isRunning = true, error = null) }
                    }
                    is com.adbcommand.app.domain.models.LogcatEvent.Line -> {
                        _uiState.update { state ->
                            val updated = (state.lines + event.logLine)
                                .takeLast(maxLines)
                            state.copy(lines = updated)
                        }
                    }
                    is com.adbcommand.app.domain.models.LogcatEvent.Stopped -> {
                        _uiState.update { it.copy(isRunning = false) }
                    }
                    is com.adbcommand.app.domain.models.LogcatEvent.Error -> {
                        _uiState.update {
                            it.copy(isRunning = false, error = event.message)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun stop() {
        logcatJob?.cancel()
        logcatJob = null
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun clear() {
        _uiState.update { it.copy(lines = emptyList(), error = null) }
    }


    private fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val rawLines = _uiState.value.lines.map { it.raw.ifBlank { it.message } }
            val result = saveLogcat(rawLines)

            _uiState.update {
                it.copy(
                    isSaving   = false,
                    saveResult = result.fold(
                        onSuccess = { path -> "Saved to $path" },
                        onFailure = { err  -> "Save failed: ${err.message}" }
                    )
                )
            }
        }
    }

    private fun restartWithFilter(newFilter: LogcatFilter) {
        val wasRunning = logcatJob?.isActive == true
        stop()
        _uiState.update { it.copy(filter = newFilter, lines = emptyList()) }
        if (wasRunning) start()
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}