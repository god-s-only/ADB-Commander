package com.adbcommand.app.data.repository

import com.adbcommand.app.core.AdbCommandsBuilder
import com.adbcommand.app.domain.models.AdbCommand
import com.adbcommand.app.domain.repository.CommandsRepository
import jakarta.inject.Inject

class CommandsRepositoryImpl @Inject constructor() : CommandsRepository {

    override fun getCommands(
        ip: String,
        adbPort: String,
        pairingPort: String,
        pairingCode: String
    ): List<AdbCommand> = AdbCommandsBuilder.build(
        ip = ip,
        adbPort = adbPort,
        pairingPort = pairingPort,
        pairingCode = pairingCode
    )
}