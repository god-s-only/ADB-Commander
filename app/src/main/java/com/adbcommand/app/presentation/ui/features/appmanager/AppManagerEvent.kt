package com.adbcommand.app.presentation.ui.features.appmanager

import com.adbcommand.app.domain.models.AppInfo

sealed class AppManagerEvent {
    object LoadApps: AppManagerEvent()
    data class SearchChanged(val query: String): AppManagerEvent()
    data class SelectApp(val app: AppInfo): AppManagerEvent()
    object DismissBottomSheet: AppManagerEvent()
    object DismissActionResult: AppManagerEvent()
    object ToggleSystemApps: AppManagerEvent()
    data class Kill(val packageName: String): AppManagerEvent()
    data class ClearData(val packageName: String): AppManagerEvent()
    data class ExtractApk(val packageName: String): AppManagerEvent()
    data class Uninstall(val packageName: String): AppManagerEvent()
    data class Launch(val packageName: String): AppManagerEvent()
}