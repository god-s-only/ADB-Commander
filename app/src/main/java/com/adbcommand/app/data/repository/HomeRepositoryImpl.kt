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

class HomeRepositoryImpl @Inject constructor(
    private val shellExecutor: ShellCommandsExecution
) : HomeRepository {

    override suspend fun getDeviceIp(): Result<String> {
        return try {
            val ip = withContext(Dispatchers.IO) {
                NetworkInterface.getNetworkInterfaces()
                    ?.asSequence()
                    ?.flatMap { it.inetAddresses.asSequence() }
                    ?.firstOrNull { address ->
                        !address.isLoopbackAddress && address is Inet4Address
                    }
                    ?.hostAddress
            } ?: return Result.failure(Exception("No IP address found"))

            Result.success(ip)
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error getting device ip", e)
            Result.failure(Exception("Error getting device ip: ${e.message}"))
        }
    }


    override suspend fun getPairingPort(): Result<String> {
        return try {
            val result = shellExecutor.run(Commands.getPairingPort())
            val port = result.output.trim()

            if (port.isBlank() || port == "0") {
                Result.failure(Exception("Pairing port not available — is Wireless Debugging enabled?"))
            } else {
                Result.success(port)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error getting pairing port", e)
            Result.failure(e)
        }
    }

    override suspend fun getAdbPort(): Result<String> {
        return try {
            val result = shellExecutor.run(Commands.getAdbPort())
            val port = result.output.trim()

            if (port.isBlank() || port == "-1" || port == "0") {
                Result.success("5555")
            } else {
                Result.success(port)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "Error getting ADB port", e)
            Result.success("5555")
        }
    }

    override suspend fun generatePairingCode(): Result<String> {
        return try {
            val result = shellExecutor.run(Commands.generatePairCode())
            val code = result.output.trim()

            if (code.isBlank()) {
                Result.failure(Exception("Code not readable — check Wireless Debugging screen"))
            } else {
                Result.success(code)
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "Unexpected error in generatePairingCode", e)
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> {
        return try {
            val result = shellExecutor.run(Commands.pingConnection())
            if (!result.success) {
                return Result.failure(
                    Exception(result.error.ifBlank { "Ping failed" })
                )
            }
            val output = result.output.ifBlank {
                return Result.failure(Exception("No response from server"))
            }
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}