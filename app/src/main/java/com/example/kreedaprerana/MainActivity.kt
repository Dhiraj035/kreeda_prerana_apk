package com.example.kreedaprerana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kreedaprerana.navigation.AppNavHost
import com.example.kreedaprerana.navigation.Routes
import com.example.kreedaprerana.navigation.shouldShowBottomNav
import com.example.kreedaprerana.ui.theme.*
import com.example.kreedaprerana.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KreedaPreranaTheme {
                val navController = rememberNavController()
                val athleteViewModel: AthleteViewModel = viewModel()
                val trialLoggerViewModel: TrialLoggerViewModel = viewModel()
                val leaderboardViewModel: LeaderboardViewModel = viewModel()
                val analyticsViewModel: AnalyticsViewModel = viewModel()
                val dashboardViewModel: DashboardViewModel = viewModel()
                val badgesViewModel: BadgesViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = shouldShowBottomNav(currentRoute)

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavHost(
                            navController = navController,
                            athleteViewModel = athleteViewModel,
                            trialLoggerViewModel = trialLoggerViewModel,
                            leaderboardViewModel = leaderboardViewModel,
                            analyticsViewModel = analyticsViewModel,
                            dashboardViewModel = dashboardViewModel,
                            badgesViewModel = badgesViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, Routes.HOME),
        BottomNavItem("Athletes", Icons.Outlined.Groups, Icons.Filled.Groups, Routes.ATHLETE_LIST),
        BottomNavItem("Trials", Icons.Outlined.Timer, Icons.Filled.Timer, Routes.TRIAL_LOGGER, isCenter = true),
        BottomNavItem("Leaderboard", Icons.Outlined.Leaderboard, Icons.Filled.Leaderboard, Routes.LEADERBOARD),
        BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person, Routes.PROFILE_SETTINGS)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                if (item.isCenter) {
                    // Floating center FAB
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                item.filledIcon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                    // Regular nav item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (selected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else (MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    val route: String,
    val isCenter: Boolean = false
)