package com.dishly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dishly.app.navigation.DishlyNavGraph
import com.dishly.app.ui.theme.DishlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DishlyTheme {
                val navController = rememberNavController()
                DishlyNavGraph(navController = navController)
            }
        }
    }
}
