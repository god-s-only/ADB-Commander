package com.adbcommand.app.presentation.ui.features.commands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adbcommand.app.domain.usecase.commands.GetAdbCommandsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class)
@HiltViewModel
class CommandsViewModel @Inject constructor(
    private val getAdbCommands: GetAdbCommandsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommandsUiState())
    val uiState: StateFlow<CommandsUiState> = _uiState.asStateFlow()
    private var ip = ""
    private var adbPort = "5555"
    private var pairingPort = ""
    private var pairingCode = ""

    private val _searchQuery = MutableStateFlow("")

    init {
        _searchQuery
            .debounce(200)
            .onEach { query -> rebuildCommands(query) }
            .launchIn(viewModelScope)
    }

    fun init(ip: String, adbPort: String, pairingPort: String, pairingCode: String) {
        this.ip = ip
        this.adbPort = adbPort
        this.pairingPort = pairingPort
        this.pairingCode = pairingCode
        rebuildCommands(searchQuery = "")
    }

    fun onEvent(event: CommandsEvent) {
        when (event) {
            is CommandsEvent.SearchChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                _searchQuery.value = event.query
            }
            is CommandsEvent.CommandCopied -> {
                _uiState.update { it.copy(copiedCommandId = event.commandId) }
            }
            is CommandsEvent.ClearCopied -> {
                _uiState.update { it.copy(copiedCommandId = null) }
            }
            is CommandsEvent.ToggleCategory -> {
                _uiState.update { state ->
                    val expanded = state.expandedCategories.toMutableSet()
                    if (event.category in expanded) expanded.remove(event.category)
                    else expanded.add(event.category)
                    state.copy(expandedCategories = expanded)
                }
            }
        }
    }
    private fun rebuildCommands(searchQuery: String) {
        val grouped = getAdbCommands(
            ip  = ip,
            adbPort = adbPort,
            pairingPort = pairingPort,
            pairingCode = pairingCode,
            searchQuery = searchQuery
        )
        _uiState.update { it.copy(groupedCommands = grouped) }
    }
}