package com.example.nb_frontend.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nb_frontend.ui.components.TabBar
import com.example.nb_frontend.ui.components.NbTab
import com.example.nb_frontend.ui.history.HistoryScreen
import com.example.nb_frontend.ui.home.HomeScreen
import com.example.nb_frontend.ui.permission.PermissionScreen
import com.example.nb_frontend.ui.settings.SettingsScreen
import com.example.nb_frontend.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in NbTab.entries.map { it.route }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                TabBar(
                    currentRoute = currentRoute,
                    onTabClick = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onFinish = {
                        navController.navigate(Routes.PERMISSION) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.PERMISSION) {
                PermissionScreen(
                    onStart = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PERMISSION) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}