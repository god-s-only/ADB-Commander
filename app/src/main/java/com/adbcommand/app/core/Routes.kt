package com.adbcommand.app.core

object Routes {
    const val HOME = "home"
    const val APP_MANAGER_SCREEN = "app_manager"
    const val DEVICE_INFO_SCREEN = "device_info"
    const val LOGCAT_SCREEN = "logcat"
    const val SETTINGS_SCREEN = "settings"
    const val CAPTURE_SCREEN = "capture"
    const val PROCESS_MONITOR_SCREEN = "process_monitor"
    const val INTENT_SENDER_SCREEN = "intent_sender"

    const val COMMANDS_SCREEN = "commands"
    const val COMMANDS_ROUTE = "commands?ip={ip}&adbPort={adbPort}&pairingPort={pairingPort}&pairingCode={pairingCode}"
    fun commandsRoute(ip: String, adbPort: String, pairingPort: String, pairingCode: String) =
        "commands?ip=$ip&adbPort=$adbPort&pairingPort=$pairingPort&pairingCode=$pairingCode"

    const val APP_INSPECTOR_SCREEN = "app_inspector"
    const val APP_INSPECTOR_ROUTE = "app_inspector?packageName={packageName}"
    fun appInspectorRoute(packageName: String) = "app_inspector?packageName=$packageName"

    const val PAYWALL_SCREEN = "paywall"
    const val PAYWALL_ROUTE = "paywall?feature={feature}"
    fun paywallRoute(feature: String = "") = "paywall?feature=$feature"
}