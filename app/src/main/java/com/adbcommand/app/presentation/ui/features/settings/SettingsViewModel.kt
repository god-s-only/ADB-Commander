package com.adbcommand.app.presentation.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.core.FeatureManager
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.models.UserEntitlement
import com.adbcommand.app.presentation.ui.features.home.ShizukuState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SettingsUiState(
    val isRestoringPurchase: Boolean = false,
    val restoreMessage: String? = null,
    val appVersion: String = "1.0.0"
)

sealed class SettingsEvent {
    object RestorePurchase: SettingsEvent()
    object RequestShizuku: SettingsEvent()
    object DismissMessage: SettingsEvent()
}



@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val shizukuManager: ShizukuManager,
    featureManager: FeatureManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuManager.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShizukuState())

    val entitlement: StateFlow<UserEntitlement> = featureManager.entitlementFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserEntitlement())

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.RequestShizuku  -> shizukuManager.requestPermission()
            is SettingsEvent.DismissMessage  ->
                _uiState.update { it.copy(restoreMessage = null) }
            else -> Unit
        }
    }


}