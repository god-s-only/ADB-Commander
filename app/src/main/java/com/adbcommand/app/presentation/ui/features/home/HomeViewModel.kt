package com.adbcommand.app.presentation.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.usecase.home.GetDeviceIpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val getDeviceIpUseCase: GetDeviceIpUseCase) : ViewModel() {
    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getDeviceIpUseCase.invoke().fold(
                onSuccess = { deviceIp ->
                    _state.update {
                        it.copy(
                            deviceIp = deviceIp
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            deviceIp = e.message ?: ""
                        )
                    }
                }
            )
        }
    }
}