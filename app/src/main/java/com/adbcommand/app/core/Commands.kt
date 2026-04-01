package com.adbcommand.app.core

object Commands {
    fun wirelessIp() =
        "ip addr show wlan0"

    fun generatePairCode() =
        "cmd bluetooth_manager generate-pairing-code"
    fun pingConnection(): String = "ping -c 1 8.8.8.8"
    fun adbConnect(ip: String, port: Int): String = "adb connect $ip:$port"

    fun adbDisconnect(ip: String, port: Int): String = "adb disconnect $ip:$port"
    fun adbDevices(): String = "adb devices"

    fun adbTcpip(port: Int = 5555): String = "adb tcpip $port"
}