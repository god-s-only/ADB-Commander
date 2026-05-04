package com.adbcommand.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adbcommand.app.core.Routes
import com.adbcommand.app.presentation.theme.ADBCommanderTheme

import com.adbcommand.app.presentation.ui.features.appmanager.AppManagerScreen
import com.adbcommand.app.presentation.ui.features.capture.CaptureScreen
import com.adbcommand.app.presentation.ui.features.commands.CommandsScreen
import com.adbcommand.app.presentation.ui.features.deviceinfo.DeviceInfoScreen
import com.adbcommand.app.presentation.ui.features.home.AdbCommanderHome
import com.adbcommand.app.presentation.ui.features.logcat.LogcatScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBCommanderTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

                val showBottomBar = currentDestination?.route?.let { route ->
                    bottomBarRoutes.any { route.startsWith(it) }
                } ?: false

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(
                            visible  = showBottomBar,
                            enter    = fadeIn() + slideInVertically { it },
                            exit     = fadeOut() + slideOutVertically { it }
                        ) {
                            AdbBottomBar(
                                navController        = navController,
                                currentDestination   = currentDestination
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController    = navController,
                        startDestination = Routes.CAPTURE_SCREEN,
                        modifier         = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {

                        composable(Routes.HOME) {
                            AdbCommanderHome(
                                onShowCommands = { ip, adbPort, pairingPort, code ->
                                    navController.navigate(
                                        Routes.COMMANDS_SCREEN +
                                                "?ip=$ip" +
                                                "&adbPort=$adbPort" +
                                                "&pairingPort=$pairingPort" +
                                                "&pairingCode=$code"
                                    )
                                }
                            )
                        }

                        composable(
                            route = Routes.COMMANDS_SCREEN +
                                    "?ip={ip}" +
                                    "&adbPort={adbPort}" +
                                    "&pairingPort={pairingPort}" +
                                    "&pairingCode={pairingCode}",
                            arguments = listOf(
                                navArgument("ip")          { defaultValue = "" },
                                navArgument("adbPort")     { defaultValue = "5555" },
                                navArgument("pairingPort") { defaultValue = "" },
                                navArgument("pairingCode") { defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            CommandsScreen(
                                ip             = backStackEntry.arguments?.getString("ip")          ?: "",
                                adbPort        = backStackEntry.arguments?.getString("adbPort")     ?: "5555",
                                pairingPort    = backStackEntry.arguments?.getString("pairingPort") ?: "",
                                pairingCode    = backStackEntry.arguments?.getString("pairingCode") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.APP_MANAGER_SCREEN) {
                            AppManagerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.DEVICE_INFO_SCREEN) {
                            DeviceInfoScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.LOGCAT_SCREEN) {
                            LogcatScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.CAPTURE_SCREEN) {
                            CaptureScreen(onNavigateBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdbBottomBar(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { destination ->
                destination.route?.startsWith(item.route) == true
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon
                        else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        fontSize   = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                }
            )
        }
    }
}

sealed class BottomNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val route: String
) {
    object Home : BottomNavItem(
        selectedIcon   = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label          = "Home",
        route          = Routes.HOME
    )

    object AppManager : BottomNavItem(
        selectedIcon   = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps,
        label          = "Apps",
        route          = Routes.APP_MANAGER_SCREEN
    )

    object DeviceInfo : BottomNavItem(
        selectedIcon   = Icons.Filled.PhoneAndroid,
        unselectedIcon = Icons.Outlined.PhoneAndroid,
        label          = "Device",
        route          = Routes.DEVICE_INFO_SCREEN
    )

    object Logcat : BottomNavItem(
        selectedIcon   = Icons.Filled.Terminal,
        unselectedIcon = Icons.Outlined.Terminal,
        label          = "Logcat",
        route          = Routes.LOGCAT_SCREEN
    )

}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.AppManager,
    BottomNavItem.DeviceInfo,
    BottomNavItem.Logcat,
)