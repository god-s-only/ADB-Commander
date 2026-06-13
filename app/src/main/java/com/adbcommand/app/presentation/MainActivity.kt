package com.adbcommand.app.presentation

import android.annotation.SuppressLint
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adbcommand.app.core.Routes
import com.adbcommand.app.domain.models.Feature
import com.adbcommand.app.presentation.theme.ADBCommanderTheme
import com.adbcommand.app.presentation.ui.components.bottomNavItems
import com.adbcommand.app.presentation.ui.components.detailRoutes
import com.adbcommand.app.presentation.ui.features.appmanager.AppManagerScreen
import com.adbcommand.app.presentation.ui.features.capture.CaptureScreen
import com.adbcommand.app.presentation.ui.features.commands.CommandsScreen
import com.adbcommand.app.presentation.ui.features.deviceinfo.DeviceInfoScreen
import com.adbcommand.app.presentation.ui.features.home.AdbCommanderHome
import com.adbcommand.app.presentation.ui.features.inspector.AppInspectorScreen
import com.adbcommand.app.presentation.ui.features.intentsender.IntentSenderScreen
import com.adbcommand.app.presentation.ui.features.logcat.LogcatScreen
import com.adbcommand.app.presentation.ui.features.paywall.PaywallScreen
import com.adbcommand.app.presentation.ui.features.processmonitor.ProcessMonitorScreen
import com.adbcommand.app.presentation.ui.features.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBCommanderTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                val currentRoute = currentDestination?.route ?: ""
                val showBottomBar = detailRoutes.none { currentRoute.startsWith(it) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it }
                        ) {
                            AdbBottomBar(navController, currentDestination)
                        }
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier.padding(it)
                    ) {
                        composable(Routes.HOME) {
                            AdbCommanderHome(
                                onShowCommands = { ip, adbPort, pairingPort, code ->
                                    navController.navigate(Routes.commandsRoute(ip, adbPort, pairingPort, code))
                                },
                                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_SCREEN) },
                                onNavigateToCapture = { navController.navigate(Routes.CAPTURE_SCREEN) },
                                onNavigateToProcessMonitor = { navController.navigate(Routes.PROCESS_MONITOR_SCREEN) },
                                onNavigateToIntentSender = { navController.navigate(Routes.INTENT_SENDER_SCREEN) }
                            )
                        }

                        composable(
                            route = Routes.COMMANDS_ROUTE,
                            arguments = listOf(
                                navArgument("ip") { defaultValue = "" },
                                navArgument("adbPort") { defaultValue = "5555" },
                                navArgument("pairingPort") { defaultValue = "" },
                                navArgument("pairingCode") { defaultValue = "" }
                            )
                        ) { entry ->
                            CommandsScreen(
                                ip = entry.arguments?.getString("ip") ?: "",
                                adbPort = entry.arguments?.getString("adbPort") ?: "5555",
                                pairingPort = entry.arguments?.getString("pairingPort") ?: "",
                                pairingCode = entry.arguments?.getString("pairingCode") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.APP_MANAGER_SCREEN) {
                            AppManagerScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToInspector = { pkg ->
                                    navController.navigate(Routes.appInspectorRoute(pkg))
                                },
                                onNavigateToPaywall = { feature ->
                                    navController.navigate(Routes.paywallRoute(feature.name))
                                }
                            )
                        }

                        composable(Routes.DEVICE_INFO_SCREEN) {
                            DeviceInfoScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { feature ->
                                    navController.navigate(Routes.paywallRoute(feature.name))
                                }
                            )
                        }

                        composable(Routes.LOGCAT_SCREEN) {
                            LogcatScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { feature ->
                                    navController.navigate(Routes.paywallRoute(feature.name))
                                }
                            )
                        }

                        composable(Routes.SETTINGS_SCREEN) {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToPaywall = { navController.navigate(Routes.paywallRoute()) }
                            )
                        }

                        composable(
                            route = Routes.PAYWALL_ROUTE,
                            arguments = listOf(navArgument("feature") { defaultValue = "" })
                        ) { entry ->
                            val featureName = entry.arguments?.getString("feature") ?: ""
                            val feature = runCatching { Feature.valueOf(featureName) }.getOrNull()
                            PaywallScreen(
                                onNavigateBack = { navController.popBackStack() },
                                highlightFeature = feature
                            )
                        }

                        composable(Routes.CAPTURE_SCREEN) {
                            CaptureScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(
                            route = Routes.APP_INSPECTOR_ROUTE,
                            arguments = listOf(navArgument("packageName") { defaultValue = "" })
                        ) { entry ->
                            AppInspectorScreen(
                                packageName = entry.arguments?.getString("packageName") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.PROCESS_MONITOR_SCREEN) {
                            ProcessMonitorScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(Routes.INTENT_SENDER_SCREEN) {
                            IntentSenderScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdbBottomBar(navController: NavHostController, currentDestination: NavDestination?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { dest ->
                dest.route?.startsWith(item.route) == true
            } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label)
                },
                label = {
                    Text(item.label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                }
            )
        }
    }
}