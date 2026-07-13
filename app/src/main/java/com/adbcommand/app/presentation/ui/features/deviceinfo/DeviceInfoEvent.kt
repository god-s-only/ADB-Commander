package com.adbcommand.app.presentation.ui.features.deviceinfo

sealed class DeviceInfoEvent {
    object Load: DeviceInfoEvent()
    object Refresh: DeviceInfoEvent()
    object CopyProfile: DeviceInfoEvent()
    object ClearCopiedStatus: DeviceInfoEvent()
}