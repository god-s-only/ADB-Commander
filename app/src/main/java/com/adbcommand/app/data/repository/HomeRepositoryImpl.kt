package com.adbcommand.app.data.repository

import android.util.Log
import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.domain.repository.HomeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class HomeRepositoryImpl @Inject constructor(private val shellExecutor: ShellCommandsExecution): HomeRepository {
    override suspend fun getDeviceIp(): Result<String> {
        return try {
            val ip = withContext(Dispatchers.IO) {
                NetworkInterface.getNetworkInterfaces()
                    ?.asSequence()
                    ?.flatMap { it.inetAddresses.asSequence() }
                    ?.firstOrNull { address ->
                        !address.isLoopbackAddress &&
                                address is Inet4Address
                    }
                    ?.hostAddress
            } ?: return Result.failure(Exception("No IP address found"))

            Result.success(ip)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error getting device ip", e)
            Result.failure(Exception("Error getting device ip: ${e.message}"))
        }
    }
}