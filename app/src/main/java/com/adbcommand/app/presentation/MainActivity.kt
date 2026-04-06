package com.adbcommand.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adbcommand.app.core.Routes
import com.adbcommand.app.presentation.theme.ADBCommanderTheme
import com.adbcommand.app.presentation.ui.features.appmanager.AppManagerScreen
import com.adbcommand.app.presentation.ui.features.commands.CommandsScreen
import com.adbcommand.app.presentation.ui.features.deviceinfo.DeviceInfoScreen
import com.adbcommand.app.presentation.ui.features.home.AdbCommanderHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBCommanderTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.HOME){
                    composable(Routes.HOME) {
                        AdbCommanderHome(onShowCommands = { ip, adbPort, pairingPort, code ->
                            navController.navigate(
                                Routes.COMMANDS_SCREEN +
                                        "?ip=$ip" +
                                        "&adbPort=$adbPort" +
                                        "&pairingPort=$pairingPort" +
                                        "&pairingCode=$code"
                            )
                        })
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
                        val ip          = backStackEntry.arguments?.getString("ip")          ?: ""
                        val adbPort     = backStackEntry.arguments?.getString("adbPort")     ?: "5555"
                        val pairingPort = backStackEntry.arguments?.getString("pairingPort") ?: ""
                        val pairingCode = backStackEntry.arguments?.getString("pairingCode") ?: ""

                        CommandsScreen(
                            ip           = ip,
                            adbPort      = adbPort,
                            pairingPort  = pairingPort,
                            pairingCode  = pairingCode,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.DEVICE_INFO_SCREEN) {
                        DeviceInfoScreen({})
                    }
                    composable(Routes.APP_MANAGER_SCREEN) {
                        AppManagerScreen({})
                    }
                }
            }
        }
    }
}