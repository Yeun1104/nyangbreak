package com.example.nb_frontend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nb_frontend.ui.home.HomeScreen
import com.example.nb_frontend.ui.permission.PermissionScreen
import com.example.nb_frontend.ui.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val PERMISSION = "permission"
    const val HOME = "home"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
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

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}