package com.letovpn.proxychecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.letovpn.proxychecker.ui.screens.HomeScreen
import com.letovpn.proxychecker.ui.screens.SettingsScreen
import com.letovpn.proxychecker.ui.theme.ProxyCheckerTheme
import com.letovpn.proxychecker.ui.viewmodel.ProxyViewModel
import com.letovpn.proxychecker.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsVM: SettingsViewModel = viewModel()
            val proxyVM: ProxyViewModel = viewModel()
            val isDarkTheme by settingsVM.isDarkTheme.collectAsState()
            ProxyCheckerTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") { HomeScreen(nav, proxyVM, settingsVM) }
                        composable("settings") { SettingsScreen(nav, settingsVM) }
                    }
                }
            }
        }
    }
}
