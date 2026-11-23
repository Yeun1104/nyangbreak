package com.example.nb_frontend.frontend.ui.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.nb_frontend.backend.service.MonitorStatus
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nb_frontend.backend.util.PermissionUtil
import com.example.nb_frontend.backend.viewmodel.HistoryViewModel
import com.example.nb_frontend.backend.viewmodel.HomeViewModel
import com.example.nb_frontend.backend.viewmodel.PermissionViewModel
import com.example.nb_frontend.backend.viewmodel.SettingsViewModel
import com.example.nb_frontend.backend.viewmodel.TrackedAppStatus
import com.example.nb_frontend.backend.viewmodel.TrackedAppsViewModel
import com.example.nb_frontend.frontend.ui.components.NbTab
import com.example.nb_frontend.frontend.ui.components.TabBar
import com.example.nb_frontend.frontend.ui.history.AppUsage
import com.example.nb_frontend.frontend.ui.history.DayUsage
import com.example.nb_frontend.frontend.ui.history.HistoryScreen
import com.example.nb_frontend.frontend.ui.home.HomeScreen
import com.example.nb_frontend.frontend.ui.permission.PermissionScreen
import com.example.nb_frontend.frontend.ui.settings.SettingsScreen
import com.example.nb_frontend.frontend.ui.settings.TrackedAppItem
import com.example.nb_frontend.frontend.ui.settings.TrackedAppsScreen
import com.example.nb_frontend.frontend.ui.splash.SplashScreen
import kotlin.math.roundToInt

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
                val context = LocalContext.current
                SplashScreen(
                    onFinish = {
                        val ready = PermissionUtil.hasRequired(context) &&
                                PermissionUtil.hasNotificationPermission(context)
                        navController.navigate(if (ready) Routes.HOME else Routes.PERMISSION) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.PERMISSION) {
                val vm: PermissionViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

                val notificationLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { vm.refresh() }

                PermissionScreen(
                    usageGranted = state.usageGranted,
                    overlayGranted = state.overlayGranted,
                    notificationGranted = state.notificationGranted,
                    onUsageClick = { context.startActivity(vm.usageAccessIntent()) },
                    onOverlayClick = { context.startActivity(vm.overlayIntent()) },
                    onNotificationClick = {
                        val permission = vm.notificationPermission
                        if (permission != null) notificationLauncher.launch(permission) else vm.refresh()
                    },
                    onStart = {
                        vm.refresh()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PERMISSION) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()

                val focus = state.apps.minWithOrNull(
                    compareBy<TrackedAppStatus>({ it.remainingMinutes }, { -it.usedMinutes })
                )

                val live by MonitorStatus.live.collectAsStateWithLifecycle()

                Box(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        continuousMinutes = focus?.usedMinutes ?: 0,
                        limitMinutes = focus?.limitMinutes ?: 60,
                        todayHours = state.todayHours,
                        todayMinutes = state.todayMinutes,
                        catCount = state.catCount,
                        appName = focus?.takeIf { it.usedMinutes > 0 }?.name,
                        isPaused = state.isPaused,
                        onPauseClick = { vm.togglePause() }
                    )
                }
            }

            composable(Routes.HISTORY) {
                val vm: HistoryViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()

                HistoryScreen(
                    todayMinutes = state.todayMinutes,
                    blockCount = state.blockCount,
                    weekly = state.weekly.map {
                        DayUsage(
                            day = it.day,
                            hours = (it.hours * 10).roundToInt() / 10f,
                            isToday = it.isToday
                        )
                    },
                    apps = state.apps.map { AppUsage(it.name, it.minutes) }
                )
            }

            composable(Routes.SETTINGS) {
                val settingsVm: SettingsViewModel = viewModel()
                val trackedVm: TrackedAppsViewModel = viewModel()
                val settings by settingsVm.settings.collectAsStateWithLifecycle()
                val tracked by trackedVm.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                SettingsScreen(
                    usageLimit = settings.defaultLimitMinutes,
                    restSeconds = settings.breakSeconds,
                    emergencyEnabled = settings.tapToDismissEnabled,
                    preAlertEnabled = settings.preAlertEnabled,
                    selectedCat = settings.catIndex,
                    onUsageLimitChange = { minutes -> settingsVm.setDefaultLimitMinutes(minutes) },
                    onRestChange = { seconds -> settingsVm.setBreakSeconds(seconds) },
                    onEmergencyToggle = { settingsVm.setTapToDismissEnabled(!settings.tapToDismissEnabled) },
                    onPreAlertToggle = { settingsVm.setPreAlertEnabled(!settings.preAlertEnabled) },
                    onCatSelect = { settingsVm.setCatIndex(it) },
                    onTrackedAppsClick = { navController.navigate(Routes.TRACKED_APPS) },
                    onApplyLimitToAll = {
                        val minutes = settings.defaultLimitMinutes
                        tracked.trackedPackages.forEach { trackedVm.setLimitMinutes(it, minutes) }
                        Toast.makeText(
                            context,
                            "추적 중인 앱 ${tracked.trackedPackages.size}개를 ${minutes}분으로 바꿨어요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            composable(Routes.TRACKED_APPS) {
                val vm: TrackedAppsViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()

                TrackedAppsScreen(
                    apps = state.apps.map { TrackedAppItem(it.packageName, it.name) },
                    trackedPackages = state.trackedPackages,
                    limitMinutes = state.limitMinutes,
                    onToggle = { pkg, enabled -> vm.setTracked(pkg, enabled) },
                    onToggleAll = { enabled -> vm.setAllTracked(enabled) },
                    onLimitChange = { pkg, minutes -> vm.setLimitMinutes(pkg, minutes) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
