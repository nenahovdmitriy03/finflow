package com.finflow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Destination("dashboard", "Главная", Icons.Rounded.Home)
    data object Analytics : Destination("analytics", "Аналитика", Icons.Rounded.BarChart)
    data object Budgets : Destination("budgets", "Бюджеты", Icons.Rounded.Savings)
    data object Goals : Destination("goals", "Цели", Icons.Rounded.Flag)
    data object Profile : Destination("profile", "Профиль", Icons.Rounded.AccountCircle)
}

val bottomNavDestinations = listOf(
    Destination.Dashboard,
    Destination.Analytics,
    Destination.Budgets,
    Destination.Goals,
    Destination.Profile,
)
