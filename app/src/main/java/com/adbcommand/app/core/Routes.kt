package com.adbcommand.app.core

object Routes {

    const val HOME = "home"
    const val APP_MANAGER_SCREEN = "app_manager"
    const val DEVICE_INFO_SCREEN = "device_info"
    const val LOGCAT_SCREEN = "logcat"
    const val COMMANDS_SCREEN = "commands"

    const val SETTINGS_SCREEN = "settings"
    const val PAYWALL_SCREEN = "paywall"
    const val CAPTURE_SCREEN = "capture"
    const val APP_INSPECTOR_SCREEN = "app_inspector"
    const val PROCESS_MONITOR_SCREEN = "process_monitor"
    const val INTENT_SENDER_SCREEN   = "intent_sender"

    const val COMMANDS_ROUTE =
        "$COMMANDS_SCREEN?ip={ip}&adbPort={adbPort}&pairingPort={pairingPort}&pairingCode={pairingCode}"

    fun commandsRoute(
        ip: String,
        adbPort: String,
        pairingPort: String,
        pairingCode: String
    ) = "$COMMANDS_SCREEN?ip=$ip&adbPort=$adbPort&pairingPort=$pairingPort&pairingCode=$pairingCode"

    const val APP_INSPECTOR_ROUTE = "$APP_INSPECTOR_SCREEN?package={packageName}"

    fun appInspectorRoute(packageName: String) =
        "$APP_INSPECTOR_SCREEN?package=$packageName"

    const val PAYWALL_ROUTE = "$PAYWALL_SCREEN?feature={feature}"

    fun paywallRoute(featureName: String = "") =
        "$PAYWALL_SCREEN?feature=$featureName"
}