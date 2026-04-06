package com.adbcommand.app.data.repository

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.data.remote.ShizukuManager
import com.adbcommand.app.domain.repository.HomeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class HomeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizuku: ShizukuManager
) : HomeRepository {

    companion object {
        private const val TAG = "ShizukuHomeRepo"
    }


    override suspend fun getDeviceIp(): Result<String> {
        return try {
            val wifiIp = getWifiIp()
            if (!wifiIp.isNullOrBlank() && wifiIp != "0.0.0.0") {
                return Result.success(wifiIp)
            }

            val networkIp = withContext(Dispatchers.IO) {
                NetworkInterface.getNetworkInterfaces()
                    ?.asSequence()
                    ?.flatMap { it.inetAddresses.asSequence() }
                    ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.hostAddress
            }
            if (!networkIp.isNullOrBlank()) {
                Result.success(networkIp)
            } else {
                Result.failure(Exception("No IP found. Make sure Wi-Fi is connected."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getDeviceIp failed", e)
            Result.failure(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun getWifiIp(): String? {
        return try {
            val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
            val raw = wm.connectionInfo?.ipAddress ?: return null
            if (raw == 0) return null
            android.text.format.Formatter.formatIpAddress(raw)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPairingPort(): Result<String> {
        if (!shizuku.isAvailable()) {
            return Result.failure(
                Exception("Enter the pairing port shown in Settings › Wireless Debugging")
            )
        }

        return try {
            val result = shizuku.run(Commands.getPairingPort())
            val port   = result.output.trim()

            Log.d(TAG, "getPairingPort via Shizuku: '$port'")

            if (port.isBlank() || port == "0") {
                Result.failure(
                    Exception("Pairing port is 0 — tap 'Pair device with pairing code' in Wireless Debugging first")
                )
            } else {
                Result.success(port)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPairingPort failed", e)
            Result.failure(e)
        }
    }


    override suspend fun getAdbPort(): Result<String> {
        if (shizuku.isAvailable()) {
            val result = shizuku.run(Commands.getAdbPort())
            val port   = result.output.trim()
            if (port.isNotBlank() && port != "-1" && port != "0") {
                return Result.success(port)
            }
        }
        return Result.success("5555")
    }

    override suspend fun generatePairingCode(): Result<String> {
        if (!shizuku.isAvailable()) {
            return Result.failure(
                Exception(
                    "Shizuku not available — read the 6-digit code from " +
                            "Settings › Wireless Debugging › Pair device with pairing code"
                )
            )
        }

        return try {
            val result = shizuku.run(Commands.generatePairCode())
            val code   = result.output.trim()
            Log.d(TAG, "getPairingCode via Shizuku: '$code'")
            if (code.isBlank()) {
                Result.failure(
                    Exception(
                        "Code is blank — make sure you tapped 'Pair device with pairing code' " +
                                "in Wireless Debugging first, then tap Generate again"
                    )
                )
            } else {
                Result.success(code)
            }
        } catch (e: Exception) {
            Log.e(TAG, "generatePairingCode failed", e)
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<String> {
        return try {
            val result = if (shizuku.isAvailable()) {
                shizuku.run(Commands.pingConnection())
            } else {
                val process = Runtime.getRuntime().exec(
                    arrayOf("sh", "-c", Commands.pingConnection())
                )
                val output = process.inputStream.bufferedReader().readText()
                val error  = process.errorStream.bufferedReader().readText()
                val exit   = process.waitFor()
                com.adbcommand.app.domain.models.ShellResult(
                    output  = output.trim(),
                    error   = error.trim(),
                    success = exit == 0
                )
            }

            if (!result.success) {
                Result.failure(Exception(result.error.ifBlank { "Ping failed" }))
            } else {
                Result.success(result.output)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}