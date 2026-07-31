package com.dishly.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dishly.app.navigation.DishlyNavGraph
import com.dishly.app.notifications.DishlyNotifications
import com.dishly.app.notifications.NotificationNavigator
import com.dishly.app.ui.theme.DishlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        enableEdgeToEdge()
        setContent {
            DishlyTheme {
                val navController = rememberNavController()
                DishlyNavGraph(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val recipeId = intent?.getIntExtra(DishlyNotifications.EXTRA_RECIPE_ID, -1) ?: -1
        if (recipeId > 0) {
            NotificationNavigator.openRecipe(recipeId)
            intent?.removeExtra(DishlyNotifications.EXTRA_RECIPE_ID)
        }
    }
}
