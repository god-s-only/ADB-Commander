package com.adbcommand.app.core

object Commands {

    fun getPairingPort(): String = "getprop service.adb.tls.port"

    fun getAdbPort(): String = "getprop service.adb.tcp.port"

    fun isWirelessDebuggingEnabled(): String = "getprop init.svc.adbd"

    fun generatePairCode(): String = "getprop service.adb.tls.pincode"
    fun pingConnection(): String = "ping -c 1 -W 3 8.8.8.8"

    fun adbPairCommand(ip: String, pairingPort: String, code: String): String =
        "adb pair $ip:$pairingPort $code"

    fun adbConnectCommand(ip: String, adbPort: String): String =
        "adb connect $ip:$adbPort"
}