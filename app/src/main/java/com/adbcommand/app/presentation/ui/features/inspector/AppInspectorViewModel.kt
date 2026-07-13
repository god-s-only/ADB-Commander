package com.adbcommand.app.presentation.ui.features.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.usecase.inspector.InspectAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppInspectorViewModel @Inject constructor(
    private val inspectApp: InspectAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectorUiState())
    val uiState: StateFlow<InspectorUiState> = _uiState.asStateFlow()

    fun onEvent(event: InspectorEvent) {
        when (event) {
            is InspectorEvent.Load -> load(event.packageName)
            is InspectorEvent.ToggleSection -> toggle(event.section)
        }
    }

    private fun load(packageName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            inspectApp(packageName).fold(
                onSuccess = { inspection ->
                    _uiState.update {
                        it.copy(isLoading = false, inspection = inspection)
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = err.message ?: "Failed to inspect app"
                        )
                    }
                }
            )
        }
    }

    private fun toggle(section: InspectorSection) {
        _uiState.update { state ->
            val next = if (state.expandedSection == section) null else section
            state.copy(expandedSection = next)
        }
    }
}