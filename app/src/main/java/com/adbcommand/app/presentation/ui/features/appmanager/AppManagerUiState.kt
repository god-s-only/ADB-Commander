package com.adbcommand.app.presentation.ui.features.appmanager

import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo

data class AppManagerUiState(
    val isLoading: Boolean = false,
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val includeSystem: Boolean = false,

    val selectedApp: AppInfo? = null,

    val actionResult: AppActionResult? = null,

    val pendingAction: AppAction? = null,

    val error: String? = null
)