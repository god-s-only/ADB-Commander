package com.adbcommand.app.core

object Commands {
    fun wirelessIp() =
        "ip addr show wlan0"

    fun generatePairCode() =
        "cmd bluetooth_manager generate-pairing-code"
}