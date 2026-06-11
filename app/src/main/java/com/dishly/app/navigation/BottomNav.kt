package com.dishly.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dishly.app.ui.theme.Magenta
import com.dishly.app.ui.theme.NavInactive
import com.dishly.app.ui.theme.PurplePrimary
import com.dishly.app.ui.theme.White

/** Describes each bottom navigation button: label, icon and the route it points to. */
sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: TabRoute) {
    data object Search : BottomNavItem("Search", Icons.Default.Search, TabRoute.Search)
    data object Favorites : BottomNavItem("Favorites", Icons.Default.Favorite, TabRoute.Favorites)
    data object Home : BottomNavItem("Home", Icons.Default.Home, TabRoute.Home)
    data object Settings : BottomNavItem("Settings", Icons.Default.Settings, TabRoute.Settings)
    data object Profile : BottomNavItem("Profile", Icons.Default.Person, TabRoute.Profile)
}

@Composable
fun BottomNavBar(navController: NavHostController, items: List<BottomNavItem>) {
    NavigationBar(containerColor = PurplePrimary) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // Keep a single instance and return to the start destination (Home)
                        // when the back button is pressed.
                        navController.graph.startDestinationRoute?.let { start ->
                            popUpTo(start) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = White,
                    selectedTextColor = White,
                    unselectedIconColor = NavInactive,
                    unselectedTextColor = NavInactive,
                    indicatorColor = Magenta
                )
            )
        }
    }
}
