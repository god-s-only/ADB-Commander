package com.adbcommand.app.domain.models

enum class Feature(
    val displayName: String,
    val description: String
) {
    VIEW_DEVICE_IP(
        displayName = "View Device IP",
        description = "See your device's current IP address"
    ),
    VIEW_ADB_COMMANDS(
        displayName = "ADB Commands",
        description = "View and copy all ADB commands for your device"
    ),
    VIEW_DEVICE_INFO(
        displayName = "Device Info",
        description = "View basic hardware and system information"
    ),
    LIST_APPS(
        displayName = "List Installed Apps",
        description = "Browse all installed apps on your device"
    ),
    LAUNCH_APP(
        displayName = "Launch App",
        description = "Open any installed app"
    ),
    VIEW_LOGCAT(
        displayName = "View Logcat",
        description = "Stream and read live system logs"
    ),
    TEST_CONNECTION(
        displayName = "Test Connection",
        description = "Verify your device's network connectivity"
    ),
    FORCE_STOP(
        displayName = "Force Stop App",
        description = "Forcefully stop any running app including foreground processes"
    ),
    CLEAR_DATA(
        displayName = "Clear App Data",
        description = "Wipe all data and cache for any app instantly"
    ),
    EXTRACT_APK(
        displayName = "Extract APK",
        description = "Save any app's APK file to your Downloads folder"
    ),
    UNINSTALL_APP(
        displayName = "Uninstall App",
        description = "Silently uninstall any app without a confirmation dialog"
    ),
    SAVE_LOGCAT(
        displayName = "Save Logcat",
        description = "Export the current log session to a file"
    ),
    LOGCAT_TAG_FILTER(
        displayName = "Logcat Tag Filter",
        description = "Filter logs by specific tag for targeted debugging"
    ),
    LOGCAT_LEVEL_FILTER(
        displayName = "Logcat Level Filter",
        description = "Filter logs by severity level (Error, Warning, etc.)"
    ),
    SHELL_COMMANDS(
        displayName = "Shell Terminal",
        description = "Run arbitrary shell commands via Shizuku"
    ),
    DEVICE_PROFILE_COPY(
        displayName = "Copy Device Profile",
        description = "Export full device specifications as a shareable text report"
    )
}

val FREE_FEATURES = setOf(
    Feature.VIEW_DEVICE_IP,
    Feature.VIEW_ADB_COMMANDS,
    Feature.VIEW_DEVICE_INFO,
    Feature.LIST_APPS,
    Feature.LAUNCH_APP,
    Feature.VIEW_LOGCAT,
    Feature.TEST_CONNECTION
)

val PRO_FEATURES = Feature.entries.toSet() - FREE_FEATURES
