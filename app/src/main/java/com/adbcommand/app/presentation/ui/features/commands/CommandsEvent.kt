package com.adbcommand.app.presentation.ui.features.commands

import com.adbcommand.app.domain.models.CommandCategory

sealed class CommandsEvent {
    data class SearchChanged(val query: String): CommandsEvent()
    data class CommandCopied(val commandId: String): CommandsEvent()
    data class ToggleCategory(val category: CommandCategory): CommandsEvent()
    object ClearCopied: CommandsEvent()
}