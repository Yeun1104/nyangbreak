package com.example.nb_frontend.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.nb_frontend.ui.settings.TrackedAppItem
import com.example.nb_frontend.ui.settings.TrackedAppsScreen
import com.example.nb_frontend.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val TRACKED_APPS = "trackedApps"
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
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onTrackedAppsClick = { navController.navigate(Routes.TRACKED_APPS) }
                )
            }

            composable(Routes.TRACKED_APPS) {
                var trackedPackages by remember { mutableStateOf(setOf<String>()) }
                val apps = listOf(
                    TrackedAppItem("com.google.android.youtube", "YouTube"),
                    TrackedAppItem("com.instagram.android", "Instagram")
                )

                TrackedAppsScreen(
                    apps = apps,
                    trackedPackages = trackedPackages,
                    onToggle = { pkg, enabled ->
                        trackedPackages = if (enabled) trackedPackages + pkg else trackedPackages - pkg
                    },
                    onToggleAll = { enabled ->
                        trackedPackages = if (enabled) apps.map { it.packageName }.toSet() else emptySet()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}