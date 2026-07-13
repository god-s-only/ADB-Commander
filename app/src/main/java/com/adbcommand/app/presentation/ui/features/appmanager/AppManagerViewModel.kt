package com.adbcommand.app.presentation.ui.features.appmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.models.AppActionResult
import com.adbcommand.app.domain.models.AppInfo
import com.adbcommand.app.domain.usecase.*
import com.adbcommand.app.domain.usecase.appmanager.ClearDataUseCase
import com.adbcommand.app.domain.usecase.appmanager.ExtractApkUseCase
import com.adbcommand.app.domain.usecase.appmanager.GetInstalledAppsUseCase
import com.adbcommand.app.domain.usecase.appmanager.KillAppUseCase
import com.adbcommand.app.domain.usecase.appmanager.LaunchAppUseCase
import com.adbcommand.app.domain.usecase.appmanager.UninstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppAction { KILL, CLEAR, EXTRACT, UNINSTALL, LAUNCH }



@OptIn(FlowPreview::class)
@HiltViewModel
class AppManagerViewModel @Inject constructor(
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val killApp: KillAppUseCase,
    private val clearData: ClearDataUseCase,
    private val extractApk: ExtractApkUseCase,
    private val uninstallApp: UninstallAppUseCase,
    private val launchApp: LaunchAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppManagerUiState())
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        _searchQuery
            .debounce(200)
            .onEach { query -> applyFilter(query) }
            .launchIn(viewModelScope)

        onEvent(AppManagerEvent.LoadApps)
    }

    fun onEvent(event: AppManagerEvent) {
        when (event) {
            is AppManagerEvent.LoadApps         -> loadApps()
            is AppManagerEvent.SearchChanged    -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                _searchQuery.value = event.query
            }
            is AppManagerEvent.SelectApp        ->
                _uiState.update { it.copy(selectedApp = event.app) }
            is AppManagerEvent.DismissBottomSheet ->
                _uiState.update { it.copy(selectedApp = null) }
            is AppManagerEvent.DismissActionResult ->
                _uiState.update { it.copy(actionResult = null) }
            is AppManagerEvent.ToggleSystemApps -> {
                val newValue = !_uiState.value.includeSystem
                _uiState.update { it.copy(includeSystem = newValue) }
                loadApps()
            }
            is AppManagerEvent.Kill      -> runAction(AppAction.KILL, event.packageName) {
                killApp(event.packageName)
            }
            is AppManagerEvent.ClearData -> runAction(AppAction.CLEAR, event.packageName) {
                clearData(event.packageName)
            }
            is AppManagerEvent.ExtractApk -> runAction(AppAction.EXTRACT, event.packageName) {
                extractApk(event.packageName)
            }
            is AppManagerEvent.Uninstall -> runAction(AppAction.UNINSTALL, event.packageName) {
                uninstallApp(event.packageName)
            }
            is AppManagerEvent.Launch    -> runAction(AppAction.LAUNCH, event.packageName) {
                launchApp(event.packageName)
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getInstalledApps(_uiState.value.includeSystem).fold(
                onSuccess = { apps ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            apps         = apps,
                            filteredApps = filterApps(apps, it.searchQuery)
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message ?: "Failed to load apps")
                    }
                }
            )
        }
    }
    private fun applyFilter(query: String) {
        _uiState.update {
            it.copy(filteredApps = filterApps(it.apps, query))
        }
    }

    private fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        return apps.filter { app ->
            app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
        }
    }

    private fun runAction(
        action: AppAction,
        packageName: String,
        block: suspend () -> AppActionResult
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(pendingAction = action) }

            val result = block()

            val updatedApps = if (action == AppAction.UNINSTALL &&
                result is AppActionResult.Success) {
                _uiState.value.apps.filter { it.packageName != packageName }
            } else {
                _uiState.value.apps
            }

            _uiState.update {
                it.copy(
                    pendingAction = null,
                    actionResult  = result,
                    selectedApp   = if (action == AppAction.UNINSTALL) null
                    else it.selectedApp,
                    apps          = updatedApps,
                    filteredApps  = filterApps(updatedApps, it.searchQuery)
                )
            }
        }
    }
}