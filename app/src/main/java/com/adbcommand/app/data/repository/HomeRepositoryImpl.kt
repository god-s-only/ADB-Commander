package com.adbcommand.app.data.repository

import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.domain.repository.HomeRepository
import jakarta.inject.Inject

class HomeRepositoryImpl @Inject constructor(private val shellExecutor: ShellCommandsExecution): HomeRepository {
    override suspend fun getDeviceIp(): Result<String> {
        return try {
            val output = shellExecutor.run(Commands.wirelessIp())
            Result.success( Regex("inet ([0-9.]+)").find(output.output)?.groupValues?.get(1) ?: "Unknown")
        }catch (e: Exception){
            Result.failure(Exception("Error getting device ip"))
        }
    }
}