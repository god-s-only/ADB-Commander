package com.adbcommand.app.core

import com.adbcommand.app.domain.models.AdbCommand
import com.adbcommand.app.domain.models.CommandCategory

object AdbCommandsBuilder {

    fun build(
        ip: String,
        adbPort: String,
        pairingPort: String,
        pairingCode: String
    ): List<AdbCommand> = buildList {

        add(AdbCommand(
            id       = "pair",
            category = CommandCategory.CONNECTION,
            title    = "Pair Device",
            command  = "adb pair $ip:$pairingPort $pairingCode",
            hint     = "Run this once on your PC to authorize wireless debugging",
            needsInput = pairingCode.isBlank()
        ))
        add(AdbCommand(
            id       = "connect",
            category = CommandCategory.CONNECTION,
            title    = "Connect to Device",
            command  = "adb connect $ip:$adbPort",
            hint     = "Run after pairing — establishes the ADB session"
        ))
        add(AdbCommand(
            id       = "tcpip",
            category = CommandCategory.CONNECTION,
            title    = "Enable TCP/IP Mode",
            command  = "adb tcpip 5555",
            hint     = "Run while USB is connected to enable wireless ADB"
        ))
        add(AdbCommand(
            id       = "devices",
            category = CommandCategory.CONNECTION,
            title    = "List Connected Devices",
            command  = "adb devices",
            hint     = "Lists all devices currently visible to your ADB server"
        ))
        add(AdbCommand(
            id       = "disconnect",
            category = CommandCategory.CONNECTION,
            title    = "Disconnect Device",
            command  = "adb disconnect $ip:$adbPort",
            hint     = "Closes the wireless ADB session"
        ))

        add(AdbCommand(
            id         = "force_stop",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Force Stop App",
            command    = "adb shell am force-stop <package.name>",
            hint       = "Replace <package.name> with e.g. com.example.app",
            needsInput = true
        ))
        add(AdbCommand(
            id         = "clear_data",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Clear App Data",
            command    = "adb shell pm clear <package.name>",
            hint       = "Wipes all data and cache — resets app to fresh install",
            needsInput = true
        ))
        add(AdbCommand(
            id         = "uninstall",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Uninstall App",
            command    = "adb uninstall <package.name>",
            hint       = "Removes the app from the device",
            needsInput = true
        ))
        add(AdbCommand(
            id         = "apk_path",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Get APK Path",
            command    = "adb shell pm path <package.name>",
            hint       = "Returns the on-device path to the installed APK",
            needsInput = true
        ))
        add(AdbCommand(
            id       = "list_packages",
            category = CommandCategory.APP_MANAGEMENT,
            title    = "List Installed Apps",
            command  = "adb shell pm list packages -3",
            hint     = "Lists all third-party (non-system) installed packages"
        ))
        add(AdbCommand(
            id         = "launch_app",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Launch App",
            command    = "adb shell monkey -p <package.name> -c android.intent.category.LAUNCHER 1",
            hint       = "Launches the app's main activity",
            needsInput = true
        ))
        add(AdbCommand(
            id         = "grant_permission",
            category   = CommandCategory.APP_MANAGEMENT,
            title      = "Grant Permission",
            command    = "adb shell pm grant <package.name> android.permission.<PERMISSION>",
            hint       = "Grants a runtime permission to an app",
            needsInput = true
        ))

        add(AdbCommand(
            id       = "device_model",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Device Model",
            command  = "adb shell getprop ro.product.model",
            hint     = "Prints the device model name"
        ))
        add(AdbCommand(
            id       = "android_version",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Android Version",
            command  = "adb shell getprop ro.build.version.release",
            hint     = "Prints the Android OS version number"
        ))
        add(AdbCommand(
            id       = "all_props",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "All System Properties",
            command  = "adb shell getprop",
            hint     = "Dumps every system property — useful for deep device info"
        ))
        add(AdbCommand(
            id       = "battery",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Battery Info",
            command  = "adb shell dumpsys battery",
            hint     = "Shows battery level, temperature, charging state"
        ))
        add(AdbCommand(
            id       = "screen_size",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Screen Size",
            command  = "adb shell wm size",
            hint     = "Prints the current display resolution"
        ))
        add(AdbCommand(
            id       = "screen_density",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Screen Density",
            command  = "adb shell wm density",
            hint     = "Prints the screen DPI"
        ))
        add(AdbCommand(
            id       = "memory_info",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Memory Info",
            command  = "adb shell dumpsys meminfo",
            hint     = "Prints RAM usage across all running processes"
        ))
        add(AdbCommand(
            id       = "wifi_info",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Wi-Fi Info",
            command  = "adb shell dumpsys wifi",
            hint     = "Shows current Wi-Fi connection details"
        ))
        add(AdbCommand(
            id       = "reboot",
            category = CommandCategory.DEVICE_SYSTEM,
            title    = "Reboot Device",
            command  = "adb reboot",
            hint     = "Reboots the device immediately"
        ))

        add(AdbCommand(
            id       = "screenshot",
            category = CommandCategory.CAPTURE,
            title    = "Take Screenshot",
            command  = "adb exec-out screencap -p > screen.png",
            hint     = "Saves a screenshot to screen.png in your current PC folder"
        ))
        add(AdbCommand(
            id       = "screen_record",
            category = CommandCategory.CAPTURE,
            title    = "Start Screen Recording",
            command  = "adb shell screenrecord /sdcard/record.mp4",
            hint     = "Records the screen — press Ctrl+C to stop"
        ))
        add(AdbCommand(
            id       = "pull_screenshot",
            category = CommandCategory.CAPTURE,
            title    = "Pull Screenshot to PC",
            command  = "adb pull /sdcard/screen.png",
            hint     = "Copies the last screenshot from device to your PC"
        ))
        add(AdbCommand(
            id       = "pull_recording",
            category = CommandCategory.CAPTURE,
            title    = "Pull Screen Recording to PC",
            command  = "adb pull /sdcard/record.mp4",
            hint     = "Copies the last screen recording from device to your PC"
        ))

        add(AdbCommand(
            id       = "logcat",
            category = CommandCategory.LOGS,
            title    = "Live Logcat",
            command  = "adb logcat",
            hint     = "Streams live device logs — press Ctrl+C to stop"
        ))
        add(AdbCommand(
            id       = "logcat_dump",
            category = CommandCategory.LOGS,
            title    = "Save Logcat to File",
            command  = "adb logcat -d > logs.txt",
            hint     = "Dumps current log buffer to logs.txt on your PC"
        ))
        add(AdbCommand(
            id         = "logcat_package",
            category   = CommandCategory.LOGS,
            title      = "Filter Logcat by Tag",
            command    = "adb logcat -s <TAG>",
            hint       = "Replace <TAG> with your app tag e.g. MyApp",
            needsInput = true
        ))
        add(AdbCommand(
            id       = "logcat_clear",
            category = CommandCategory.LOGS,
            title    = "Clear Logcat Buffer",
            command  = "adb logcat -c",
            hint     = "Clears all existing log entries"
        ))
        add(AdbCommand(
            id       = "bugreport",
            category = CommandCategory.LOGS,
            title    = "Generate Bug Report",
            command  = "adb bugreport bugreport.zip",
            hint     = "Creates a full system bug report zip on your PC"
        ))
    }
}