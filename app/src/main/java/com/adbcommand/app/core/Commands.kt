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

    fun model() = "getprop ro.product.model"
    fun manufacturer() = "getprop ro.product.manufacturer"
    fun androidVersion() = "getprop ro.build.version.release"
    fun apiLevel() = "getprop ro.build.version.sdk"
    fun buildNumber() = "getprop ro.build.display.id"
    fun securityPatch() = "getprop ro.build.version.security_patch"

    fun screenSize() = "wm size"
    fun screenDensity() = "wm density"
    fun cpuAbi() = "getprop ro.product.cpu.abi"

    fun batteryDump() = "dumpsys battery"

    fun wifiDump() = "dumpsys wifi | grep 'mWifiInfo'"
}