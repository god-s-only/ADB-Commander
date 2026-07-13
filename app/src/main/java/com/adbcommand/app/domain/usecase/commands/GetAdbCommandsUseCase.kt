package com.adbcommand.app.domain.usecase.commands

import com.adbcommand.app.domain.models.AdbCommand
import com.adbcommand.app.domain.models.CommandCategory
import com.adbcommand.app.domain.repository.CommandsRepository
import jakarta.inject.Inject

class GetAdbCommandsUseCase @Inject constructor(
    private val repository: CommandsRepository
) {
    operator fun invoke(
        ip: String,
        adbPort: String,
        pairingPort: String,
        pairingCode: String,
        searchQuery: String = ""
    ): Map<CommandCategory, List<AdbCommand>> {
        val all = repository.getCommands(ip, adbPort, pairingPort, pairingCode)

        val filtered = if (searchQuery.isBlank()) all
        else all.filter { cmd ->
            cmd.title.contains(searchQuery, ignoreCase = true) ||
                    cmd.command.contains(searchQuery, ignoreCase = true) ||
                    cmd.hint.contains(searchQuery, ignoreCase = true)
        }

        return CommandCategory.entries
            .associateWith { category -> filtered.filter { it.category == category } }
            .filterValues { it.isNotEmpty() }
    }
}