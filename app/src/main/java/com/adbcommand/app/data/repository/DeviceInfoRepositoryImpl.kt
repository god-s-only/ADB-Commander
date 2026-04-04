package com.adbcommand.app.data.repository

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.adbcommand.app.core.Commands
import com.adbcommand.app.core.ShellCommandsExecution
import com.adbcommand.app.domain.models.DeviceInfo
import com.adbcommand.app.domain.repository.DeviceInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.Inet4Address
import java.net.NetworkInterface

class DeviceInfoRepositoryImpl @Inject constructor(
    private val shell: ShellCommandsExecution,
    @ApplicationContext private val context: Context
) : DeviceInfoRepository {

    companion object {
        private const val TAG = "DeviceInfoRepository"
    }

    override suspend fun getDeviceInfo(): Result<DeviceInfo> = coroutineScope {
        try {
            val modelD          = async { shell.run(Commands.model()) }
            val manufacturerD   = async { shell.run(Commands.manufacturer()) }
            val androidVersionD = async { shell.run(Commands.androidVersion()) }
            val apiLevelD       = async { shell.run(Commands.apiLevel()) }
            val buildNumberD    = async { shell.run(Commands.buildNumber()) }
            val securityPatchD  = async { shell.run(Commands.securityPatch()) }
            val screenSizeD     = async { shell.run(Commands.screenSize()) }
            val screenDensityD  = async { shell.run(Commands.screenDensity()) }
            val cpuAbiD         = async { shell.run(Commands.cpuAbi()) }
            val batteryD        = async { shell.run(Commands.batteryDump()) }
            val wifiD           = async { shell.run(Commands.wifiDump()) }

            val batteryRaw  = batteryD.await().output
            val screenSizeRaw = screenSizeD.await().output
            val screenDensityRaw = screenDensityD.await().output
            val wifiRaw     = wifiD.await().output

            Result.success(
                DeviceInfo(
                    model          = modelD.await().output.nullIfBlank(),
                    manufacturer   = manufacturerD.await().output.nullIfBlank(),
                    androidVersion = androidVersionD.await().output.nullIfBlank(),
                    apiLevel       = apiLevelD.await().output.nullIfBlank(),
                    buildNumber    = buildNumberD.await().output.nullIfBlank(),
                    securityPatch  = securityPatchD.await().output.nullIfBlank(),
                    screenSize     = parseScreenSize(screenSizeRaw),
                    screenDensity  = parseScreenDensity(screenDensityRaw),
                    cpuAbi         = cpuAbiD.await().output.nullIfBlank(),
                    totalRam       = getTotalRam(),
                    batteryLevel   = parseBatteryField(batteryRaw, "level"),
                    batteryStatus  = parseBatteryStatus(batteryRaw),
                    batteryHealth  = parseBatteryHealth(batteryRaw),
                    batteryTemp    = parseBatteryTemp(batteryRaw),
                    batteryVoltage = parseBatteryField(batteryRaw, "voltage")
                        ?.let { "${it}mV" },
                    ipAddress      = getDeviceIp(),
                    wifiState      = parseWifiState(wifiRaw)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load device info", e)
            Result.failure(e)
        }
    }
    private fun parseScreenSize(raw: String): String? {
        val match = Regex("(\\d+x\\d+)").find(raw) ?: return null
        return match.value.replace("x", " x ")
    }
    private fun parseScreenDensity(raw: String): String? {
        val match = Regex("(\\d+)").find(raw) ?: return null
        return "${match.value} dpi"
    }

    private fun parseBatteryField(raw: String, field: String): String? {
        return raw.lines()
            .firstOrNull { it.trim().startsWith("$field:") }
            ?.substringAfter(":")
            ?.trim()
            .nullIfBlank()
    }

    private fun parseBatteryStatus(raw: String): String? {
        val code = parseBatteryField(raw, "status")?.toIntOrNull() ?: return null
        return when (code) {
            2    -> "Charging"
            3    -> "Discharging"
            4    -> "Not Charging"
            5    -> "Full"
            else -> "Unknown"
        }
    }

    private fun parseBatteryHealth(raw: String): String? {
        val code = parseBatteryField(raw, "health")?.toIntOrNull() ?: return null
        return when (code) {
            2    -> "Good"
            3    -> "Overheat"
            4    -> "Dead"
            5    -> "Over Voltage"
            7    -> "Cold"
            else -> "Unknown"
        }
    }

    private fun parseBatteryTemp(raw: String): String? {
        val raw10 = parseBatteryField(raw, "temperature")?.toIntOrNull() ?: return null
        val celsius = raw10 / 10.0
        return "$celsius °C"
    }
    private fun parseWifiState(raw: String): String? {
        return when {
            raw.contains("state: CONNECTED", ignoreCase = true)    -> "Connected"
            raw.contains("state: DISCONNECTED", ignoreCase = true) -> "Disconnected"
            raw.contains("enabled",  ignoreCase = true)            -> "Enabled"
            raw.contains("disabled", ignoreCase = true)            -> "Disabled"
            else                                                    -> null
        }
    }
    private fun getTotalRam(): String? {
        return try {
            val am   = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val gb = info.totalMem / (1024.0 * 1024.0 * 1024.0)
            "%.1f GB".format(gb)
        } catch (e: Exception) {
            null
        }
    }

    private fun getDeviceIp(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.asSequence()
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
        } catch (e: Exception) {
            null
        }
    }

    private fun String?.nullIfBlank(): String? =
        if (isNullOrBlank()) null else this
}