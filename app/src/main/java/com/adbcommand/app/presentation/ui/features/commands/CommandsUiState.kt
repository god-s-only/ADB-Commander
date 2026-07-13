package com.adbcommand.app.presentation.ui.features.commands

import com.adbcommand.app.domain.models.AdbCommand
import com.adbcommand.app.domain.models.CommandCategory

data class CommandsUiState(
    val groupedCommands: Map<CommandCategory, List<AdbCommand>> = emptyMap(),
    val searchQuery: String     = "",
    val copiedCommandId: String? = null,
    val expandedCategories: Set<CommandCategory> = CommandCategory.entries.toSet()
)