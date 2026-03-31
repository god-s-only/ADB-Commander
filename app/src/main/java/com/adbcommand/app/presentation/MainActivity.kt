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
import com.adbcommand.app.core.Routes
import com.adbcommand.app.presentation.theme.ADBCommanderTheme
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
                        AdbCommanderHome()
                    }
                }
            }
        }
    }
}