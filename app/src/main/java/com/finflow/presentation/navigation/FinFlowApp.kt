package com.finflow.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.finflow.presentation.ui.analytics.AnalyticsScreen
import com.finflow.presentation.ui.budgets.BudgetsScreen
import com.finflow.presentation.ui.dashboard.DashboardScreen
import com.finflow.presentation.ui.goals.GoalsScreen
import com.finflow.presentation.ui.profile.ProfileScreen
import com.finflow.presentation.ui.transaction.AddTransactionScreen

private const val ROUTE_ADD_TRANSACTION = "add_transaction"

@Composable
fun FinFlowApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavDestinations.map { it.route }) {
                NavigationBar {
                    bottomNavDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Dashboard.route) {
                DashboardScreen(onAddTransaction = { navController.navigate(ROUTE_ADD_TRANSACTION) })
            }
            composable(Destination.Analytics.route) { AnalyticsScreen() }
            composable(Destination.Budgets.route) { BudgetsScreen() }
            composable(Destination.Goals.route) { GoalsScreen() }
            composable(Destination.Profile.route) { ProfileScreen() }
            composable(ROUTE_ADD_TRANSACTION) {
                AddTransactionScreen(
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
        }
    }
}
