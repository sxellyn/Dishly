package com.dishly.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dishly.app.ui.screens.*

@Composable
fun DishlyNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.Splash) {
        composable<Route.Splash> {
            SplashScreen(
                onNavigate = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Onboarding> {
            OnboardingScreen(onStart = { navController.navigate(Route.SignIn) })
        }
        composable<Route.SignIn> {
            SignInScreen(
                onSignIn = {
                    navController.navigate(Route.Main) {
                        popUpTo(Route.SignIn) { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate(Route.SignUp) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.SignUp> {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Route.Main) {
                        popUpTo(Route.SignIn) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Main> {
            MainScreen(
                onRecipeClick = { id -> navController.navigate(Route.Recipe(id)) },
                onEditProfile = { navController.navigate(Route.EditProfile) },
                onLogout = {
                    navController.navigate(Route.SignIn) {
                        popUpTo(Route.Main) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Recipe> { entry ->
            val recipe = entry.toRoute<Route.Recipe>()
            RecipeDetailScreen(
                recipeId = recipe.recipeId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.EditProfile> {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}
