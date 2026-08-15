package com.letovpn.proxychecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose
import androidx.navigation.compose.NavHostimportposableimportletovpn.proxychecker.ui.screens.HomeScreen
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
            ProxyCheckerTheme(darkTheme = settingsViewModel.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home",
                        enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
                        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
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
