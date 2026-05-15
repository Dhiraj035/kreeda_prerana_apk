package com.example.kreedaprerana.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kreedaprerana.ui.screens.*
import com.example.kreedaprerana.viewmodel.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val ADD_ATHLETE = "add_athlete"
    const val ATHLETE_LIST = "athlete_list"
    const val TRIAL_LOGGER = "trial_logger"
    const val LEADERBOARD = "leaderboard"
    const val ANALYTICS = "analytics"
    const val BADGES = "badges"
    const val ATHLETE_PROFILE = "athlete_profile/{athleteId}"
    const val ATHLETE_STATS = "athlete_stats/{athleteId}"
    const val PROFILE_SETTINGS = "profile_settings"

    /** Helper to build the profile route with an athlete ID. */
    fun athleteProfile(athleteId: String) = "athlete_profile/$athleteId"
    /** Helper to build the stats route with an athlete ID. */
    fun athleteStats(athleteId: String) = "athlete_stats/$athleteId"
}

/** Routes that should show the bottom navigation bar */
val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.ATHLETE_LIST,
    Routes.TRIAL_LOGGER,
    Routes.LEADERBOARD,
    Routes.ANALYTICS,
    Routes.BADGES,
    Routes.PROFILE_SETTINGS
)

/** Check if current route should show bottom nav (handles parameterized routes) */
fun shouldShowBottomNav(route: String?): Boolean {
    if (route == null) return false
    if (route in bottomNavRoutes) return true
    // Profile screen with argument should also show nav
    if (route.startsWith("athlete_profile/")) return true
    if (route.startsWith("athlete_stats/")) return true
    return false
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    athleteViewModel: AthleteViewModel,
    trialLoggerViewModel: TrialLoggerViewModel,
    leaderboardViewModel: LeaderboardViewModel,
    analyticsViewModel: AnalyticsViewModel,
    dashboardViewModel: DashboardViewModel,
    badgesViewModel: BadgesViewModel,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = { navController.navigate(Routes.SIGNUP) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true } // Clear entire backstack on login
                    }
                }
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onSignupSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true } // Clear entire backstack on signup
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onAddAthlete = { navController.navigate(Routes.ADD_ATHLETE) },
                onViewAthletes = { navController.navigate(Routes.ATHLETE_LIST) },
                onTrialLogger = { navController.navigate(Routes.TRIAL_LOGGER) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onAnalytics = { navController.navigate(Routes.ANALYTICS) },
                onBadges = { navController.navigate(Routes.BADGES) },
                dashboardViewModel = dashboardViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Routes.ADD_ATHLETE) {
            AddAthleteScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                viewModel = athleteViewModel
            )
        }
        composable(Routes.ATHLETE_LIST) {
            AthleteListScreen(
                onBack = { navController.popBackStack() },
                viewModel = athleteViewModel,
                onAthleteClick = { athleteId ->
                    navController.navigate(Routes.athleteProfile(athleteId))
                }
            )
        }
        composable(Routes.TRIAL_LOGGER) {
            TrialLoggerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                viewModel = trialLoggerViewModel
            )
        }
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                onBack = { navController.popBackStack() },
                viewModel = leaderboardViewModel
            )
        }
        composable(Routes.ANALYTICS) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() },
                viewModel = analyticsViewModel
            )
        }
        composable(Routes.BADGES) {
            BadgesScreen(
                onBack = { navController.popBackStack() },
                viewModel = badgesViewModel
            )
        }
        composable(
            route = Routes.ATHLETE_PROFILE,
            arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val athleteId = backStackEntry.arguments?.getString("athleteId") ?: ""
            val profileViewModel: AthleteProfileViewModel = viewModel()

            // Load data when screen opens
            LaunchedEffect(athleteId) {
                profileViewModel.loadAthlete(athleteId)
            }

            AthleteProfileScreen(
                onBack = { navController.popBackStack() },
                viewModel = profileViewModel,
                onViewFullStats = {
                    navController.navigate(Routes.athleteStats(athleteId))
                }
            )
        }
        composable(
            route = Routes.ATHLETE_STATS,
            arguments = listOf(navArgument("athleteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val athleteId = backStackEntry.arguments?.getString("athleteId") ?: ""
            val statsViewModel: AthleteProfileViewModel = viewModel()

            LaunchedEffect(athleteId) {
                statsViewModel.loadAthlete(athleteId)
            }

            AthleteStatsScreen(
                onBack = { navController.popBackStack() },
                viewModel = statsViewModel
            )
        }
        composable(Routes.PROFILE_SETTINGS) {
            ProfileSettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true } // Clear stack and go to login
                    }
                },
                viewModel = authViewModel
            )
        }
    }
}
