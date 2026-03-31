package com.adbcommand.app.core

object Commands {
    fun wirelessIp() =
        "ip addr show wlan0"

    fun generatePairCode() =
        "cmd bluetooth_manager generate-pairing-code"

    fun pingConnection() =
        "ping -c 1 8.8.8.8"
}