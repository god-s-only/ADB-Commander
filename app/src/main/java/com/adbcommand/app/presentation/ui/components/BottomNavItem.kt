package com.adbcommand.app.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import com.adbcommand.app.core.Routes

sealed class BottomNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val route: String
) {
    object Home : BottomNavItem(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Home",
        route = Routes.HOME
    )
    object AppManager : BottomNavItem(
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps,
        label = "Apps",
        route = Routes.APP_MANAGER_SCREEN
    )
    object DeviceInfo : BottomNavItem(
        selectedIcon = Icons.Filled.PhoneAndroid,
        unselectedIcon = Icons.Outlined.PhoneAndroid,
        label = "Device",
        route = Routes.DEVICE_INFO_SCREEN
    )
    object Logcat : BottomNavItem(
        selectedIcon = Icons.Filled.Terminal,
        unselectedIcon = Icons.Outlined.Terminal,
        label = "Logcat",
        route = Routes.LOGCAT_SCREEN
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.AppManager,
    BottomNavItem.DeviceInfo,
    BottomNavItem.Logcat
)

val detailRoutes = listOf(
    Routes.COMMANDS_SCREEN,
    Routes.APP_INSPECTOR_SCREEN,
    Routes.SETTINGS_SCREEN,
    Routes.CAPTURE_SCREEN,
    Routes.PROCESS_MONITOR_SCREEN,
    Routes.INTENT_SENDER_SCREEN,
    Routes.PAYWALL_SCREEN
)