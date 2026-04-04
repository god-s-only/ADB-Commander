package com.adbcommand.app.domain.repository

import com.adbcommand.app.domain.models.AdbCommand

interface CommandsRepository {
    fun getCommands(
        ip: String,
        adbPort: String,
        pairingPort: String,
        pairingCode: String
    ): List<AdbCommand>
}