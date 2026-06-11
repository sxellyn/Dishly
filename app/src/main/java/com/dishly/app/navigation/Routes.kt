package com.dishly.app.navigation

import kotlinx.serialization.Serializable

/** Type-safe routes for the top-level app navigation (NavHost). */
sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object Onboarding : Route
    @Serializable data object SignIn : Route
    @Serializable data object SignUp : Route
    @Serializable data object Main : Route
    @Serializable data class Recipe(val recipeId: Int) : Route
    @Serializable data object EditProfile : Route
}

/** Type-safe routes used by the bottom navigation tabs (nested NavHost). */
sealed interface TabRoute {
    @Serializable data object Home : TabRoute
    @Serializable data object Search : TabRoute
    @Serializable data object Favorites : TabRoute
    @Serializable data object Settings : TabRoute
    @Serializable data object Profile : TabRoute
}
