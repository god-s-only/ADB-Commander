package com.adbcommand.app.presentation.ui.features.deviceinfo

import com.adbcommand.app.domain.models.DeviceInfo

data class DeviceInfoUiState(
    val isLoading: Boolean   = false,
    val deviceInfo: DeviceInfo? = null,
    val error: String?       = null,
    val profileCopied: Boolean = false
)
