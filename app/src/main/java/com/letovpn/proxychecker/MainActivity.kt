package com.letovpn.proxychecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.letovpn.proxychecker.ui.screens.SourcesScreen
import com.letovpn.proxychecker.ui.theme.ProxyCheckerTheme
import com.letovpn.proxychecker.ui.viewmodel.ProxyViewModel
import com.letovpn.proxychecker.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val proxyViewModel: ProxyViewModel = viewModel()
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            ProxyCheckerTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { it } },
                        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { it } },
                        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { -it } },
                        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it } }
                    ) {
                        composable("home") { HomeScreen(navController = navController, viewModel = proxyViewModel, settingsViewModel = settingsViewModel) }
                        composable("settings") { SettingsScreen(navController = navController, viewModel = settingsViewModel) }
                        composable("sources") { SourcesScreen(navController = navController, viewModel = proxyViewModel) }
                    }
                }
            }
        }
    }
}
