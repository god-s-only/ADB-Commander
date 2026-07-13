package com.adbcommand.app.presentation.ui.features.processmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.usecase.process.GetStreamProcessesUseCase
import com.adbcommand.app.domain.models.ProcessInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessMonitorUiState(
    val processes: List<ProcessInfo>  = emptyList(),
    val isRunning: Boolean = false,
    val searchQuery: String = "",
    val showUserOnly: Boolean = false,
    val error: String? = null
)

sealed class ProcessMonitorEvent {
    object Start: ProcessMonitorEvent()
    object Stop: ProcessMonitorEvent()
    data class SearchChanged(val q: String): ProcessMonitorEvent()
    object ToggleUserOnly: ProcessMonitorEvent()
}

@HiltViewModel
class ProcessMonitorViewModel @Inject constructor(
    private val getStreamProcessesUseCase: GetStreamProcessesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessMonitorUiState())
    val uiState: StateFlow<ProcessMonitorUiState> = _uiState.asStateFlow()

    private var monitorJob: Job? = null

    fun onEvent(event: ProcessMonitorEvent) {
        when (event) {
            is ProcessMonitorEvent.Start -> start()
            is ProcessMonitorEvent.Stop -> stop()
            is ProcessMonitorEvent.SearchChanged ->
                _uiState.update { it.copy(searchQuery = event.q) }
            is ProcessMonitorEvent.ToggleUserOnly ->
                _uiState.update { it.copy(showUserOnly = !it.showUserOnly) }
        }
    }

    private fun start() {
        if (monitorJob?.isActive == true) return
        _uiState.update { it.copy(isRunning = true, error = null) }

        monitorJob = viewModelScope.launch {
            try {
                getStreamProcessesUseCase.invoke().collect { list ->
                    _uiState.update { it.copy(processes = list) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false, error = e.message) }
            }
        }
    }

    private fun stop() {
        monitorJob?.cancel()
        monitorJob = null
        _uiState.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}