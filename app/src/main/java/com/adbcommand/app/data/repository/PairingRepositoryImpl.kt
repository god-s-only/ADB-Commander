package com.adbcommand.app.data.repository

import android.util.Log
import com.adbcommand.app.core.AdbKeyStoreManager
import com.adbcommand.app.core.AdbPairingClient
import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.PairingResult
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.domain.models.PairingCredentials
import com.adbcommand.app.domain.models.PairingResult
import com.adbcommand.app.domain.repository.PairingRepository
import jakarta.inject.Inject

/**
 * Concrete implementation of [PairingRepository].
 *
 * Pairing flow
 * ────────────
 * 1. User supplies IP, pairing-port and 6-digit code (from Wireless Debugging).
 * 2. [AdbPairingClient] opens a TLS socket to <ip>:<pairingPort> and runs the
 *    SPAKE2+ exchange.  On success the device saves our RSA public key.
 * 3. The device opens its ADB port (default 5555).
 * 4. We issue `adb connect <ip>:<adbPort>` so the ADB daemon acknowledges the
 *    connection.
 */
class PairingRepositoryImpl @Inject constructor(
    private val keyStoreManager: AdbKeyStoreManager,
    private val shellExecutor: ShellCommandsExecution
) : PairingRepository {

    companion object {
        private const val TAG = "PairingRepository"
        private const val DEFAULT_ADB_PORT = 5555
    }

    private val pairingClient: AdbPairingClient by lazy {
        AdbPairingClient(
            keyStore         = keyStoreManager.keyStore,
            keyStorePassword = keyStoreManager.keyStorePassword
        )
    }

    // ── PairingRepository ────────────────────────────────────────────────────

    override suspend fun pairDevice(credentials: PairingCredentials): PairingResult {
        Log.d(TAG, "Starting pairing → ${credentials.ipAddress}:${credentials.port}")

        return try {
            val success = pairingClient.pair(
                ip          = credentials.ipAddress,
                port        = credentials.port,
                pairingCode = credentials.pairingCode
            )

            if (success) {
                Log.i(TAG, "Pairing succeeded")
                PairingResult.Success(adbPort = DEFAULT_ADB_PORT)
            } else {
                Log.w(TAG, "Device rejected the pairing code")
                PairingResult.InvalidCode
            }
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Could not reach device", e)
            PairingResult.NetworkError("Could not connect to ${credentials.ipAddress}:${credentials.port}")
        } catch (e: javax.net.ssl.SSLException) {
            Log.e(TAG, "TLS error during pairing", e)
            PairingResult.NetworkError("TLS handshake failed: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during pairing", e)
            PairingResult.UnknownError(e.message ?: "Unknown error")
        }
    }

    override suspend fun connectDevice(ipAddress: String, adbPort: Int): Result<String> {
        return try {
            val cmd    = Commands.adbConnect(ipAddress, adbPort)
            val result = shellExecutor.run(cmd)

            if (result.success) {
                Log.i(TAG, "adb connect OK: ${result.output}")
                Result.success(result.output)
            } else {
                Log.w(TAG, "adb connect failed: ${result.error}")
                Result.failure(Exception(result.error.ifBlank { "adb connect failed" }))
            }
        } catch (e: Exception) {
            Log.e(TAG, "connectDevice threw", e)
            Result.failure(e)
        }
    }

    override suspend fun disconnectDevice(ipAddress: String, adbPort: Int): Result<String> {
        return try {
            val cmd    = Commands.adbDisconnect(ipAddress, adbPort)
            val result = shellExecutor.run(cmd)

            if (result.success) Result.success(result.output)
            else Result.failure(Exception(result.error.ifBlank { "adb disconnect failed" }))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}