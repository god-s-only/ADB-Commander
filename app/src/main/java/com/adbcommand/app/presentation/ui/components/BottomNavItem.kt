package com.adbcommand.app.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.adbcommand.app.core.Routes

sealed class BottomNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val route: String
) {
    object Home : BottomNavItem(
        Icons.Filled.Home,      Icons.Outlined.Home,
        "Home",
        Routes.HOME
    )
    object AppManager : BottomNavItem(
        Icons.Filled.Apps,      Icons.Outlined.Apps,
        "Apps",
        Routes.APP_MANAGER_SCREEN
    )
    object DeviceInfo : BottomNavItem(
        Icons.Filled.PhoneAndroid, Icons.Outlined.PhoneAndroid,
        "Device",
        Routes.DEVICE_INFO_SCREEN
    )
    object Logcat : BottomNavItem(
        Icons.Filled.Terminal,  Icons.Outlined.Terminal,
        "Logcat",
        Routes.LOGCAT_SCREEN
    )
    object Commands : BottomNavItem(
        Icons.Filled.Code,      Icons.Outlined.Code,
        "Commands",
        Routes.COMMANDS_SCREEN
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.AppManager,
    BottomNavItem.DeviceInfo,
    BottomNavItem.Logcat,
    BottomNavItem.Commands
)

val detailRoutes = setOf(
    Routes.SETTINGS_SCREEN,
    Routes.PAYWALL_SCREEN,
    Routes.CAPTURE_SCREEN,
    Routes.APP_INSPECTOR_SCREEN,
    Routes.PROCESS_MONITOR_SCREEN,
    Routes.INTENT_SENDER_SCREEN,
    Routes.COMMANDS_SCREEN
)