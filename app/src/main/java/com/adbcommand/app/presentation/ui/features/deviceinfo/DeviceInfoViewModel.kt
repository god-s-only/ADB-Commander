package com.adbcommand.app.presentation.ui.features.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.usecase.deviceinfo.GetDeviceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val getDeviceInfo: GetDeviceInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceInfoUiState())
    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    init {
        onEvent(DeviceInfoEvent.Load)
    }

    fun onEvent(event: DeviceInfoEvent) {
        when (event) {
            is DeviceInfoEvent.Load,
            is DeviceInfoEvent.Refresh -> loadInfo()
            is DeviceInfoEvent.CopyProfile -> {
                _uiState.update { it.copy(profileCopied = true) }
            }
            is DeviceInfoEvent.ClearCopiedStatus -> {
                _uiState.update { it.copy(profileCopied = false) }
            }
        }
    }

    private fun loadInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getDeviceInfo().fold(
                onSuccess = { info ->
                    _uiState.update {
                        it.copy(isLoading = false, deviceInfo = info)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = error.message ?: "Failed to load device info"
                        )
                    }
                }
            )
        }
    }
}