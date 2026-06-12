package com.dishly.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dishly.app.navigation.BottomNavBar
import com.dishly.app.navigation.BottomNavItem
import com.dishly.app.navigation.TabRoute
import com.dishly.app.ui.components.*
import com.dishly.app.ui.theme.*
import com.dishly.app.model.Recipe
import com.dishly.app.viewmodel.*

@Composable
private fun RecipeGrid(recipes: List<Recipe>, onRecipeClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        recipes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { recipe ->
                    RecipeCard(
                        recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MainScreen(
    onRecipeClick: (Int) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val tabNavController = rememberNavController()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val onHome = navBackStackEntry?.destination?.hierarchy?.any { it.hasRoute(TabRoute.Home::class) } ?: true

    val goToHome: () -> Unit = {
        tabNavController.navigate(TabRoute.Home) {
            tabNavController.graph.startDestinationRoute?.let { start ->
                popUpTo(start) { saveState = true }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    BackHandler { if (!onHome) goToHome() else activity?.finish() }

    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Favorites,
        BottomNavItem.Home,
        BottomNavItem.Settings,
        BottomNavItem.Profile
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { BottomNavBar(navController = tabNavController, items = items) }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = TabRoute.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable<TabRoute.Home> { HomeScreen(onRecipeClick) }
            composable<TabRoute.Search> { SearchScreen(onRecipeClick, onBack = goToHome) }
            composable<TabRoute.Favorites> { FavoritesScreen(onRecipeClick, onBack = goToHome) }
            composable<TabRoute.Settings> { SettingsScreen(onBack = goToHome) }
            composable<TabRoute.Profile> { ProfileScreen(onEditProfile, onLogout, onBack = goToHome) }
        }
    }
}

@Composable
fun HomeScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            SectionTitle("Popular Recipes")
            SectionSubtitle("the top favorites of everybody in one place!")
            RecipeGrid(state.popularRecipes, onRecipeClick)
            Spacer(Modifier.height(16.dp))
            SectionTitle("Latest Recipes")
            SectionSubtitle("want to cook it again? here they are!")
            RecipeGrid(state.latestRecipes, onRecipeClick)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onRecipeClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(onBack = if (state.showResults) null else onBack)
        if (!state.showResults) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                SectionTitle("Find the smarter recipes for cooking")
                Spacer(Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PurplePrimary)
                        .padding(18.dp)
                ) {
                    Text(
                        "Select the ingredients you have:",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        placeholder = { Text("Search ingredients…", color = TextGray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = Magenta,
                            unfocusedBorderColor = White,
                            cursorColor = PurplePrimary,
                            focusedTextColor = PurplePrimary,
                            unfocusedTextColor = PurplePrimary
                        )
                    )
                    if (state.ingredientSuggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(White)
                        ) {
                            state.ingredientSuggestions.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    color = PurplePrimary,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectIngredient(suggestion) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                if (suggestion != state.ingredientSuggestions.last()) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White.copy(alpha = 0.2f))
                            .padding(12.dp)
                            .heightIn(min = 72.dp)
                    ) {
                        Text(
                            "Selected ingredients:",
                            color = White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        if (state.selectedIngredientNames.isEmpty()) {
                            Text(
                                "No ingredients selected yet.",
                                color = White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedIngredientNames.forEach { name ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Magenta)
                                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, color = White, fontSize = 13.sp)
                                        IconButton(
                                            onClick = { viewModel.removeSelectedIngredient(name) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove $name",
                                                tint = White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.padding(top = 20.dp)) {
                    DishlyPrimaryButton(
                        text = "Empty Fridge!",
                        onClick = viewModel::toggleEmptyFridge,
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (state.emptyFridgeMode) Magenta else PurplePrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    DishlyPrimaryButton(
                        text = "Next",
                        onClick = viewModel::search,
                        modifier = Modifier.weight(1f),
                        backgroundColor = PurplePrimary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                item {
                    Text(
                        "← Select ingredients",
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.backToPicker() }
                    )
                    SectionTitle("Based on your ingredients:")
                    SectionSubtitle("Check out these recipes:")
                }
                items(state.results) { recipe ->
                    RecipeListItem(recipe, onClick = { onRecipeClick(recipe.id) })
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onRecipeClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle("Your Favorite Recipes")
            SectionSubtitle("check your must cooks here!")
        }
        if (state.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have no favorite recipes yet.", color = TextGray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gridItems(state.favorites) { recipe ->
                    RecipeCard(recipe, onClick = { onRecipeClick(recipe.id) })
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    var notificationsEnabled by remember { mutableStateOf(hasNotificationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        Toast.makeText(
            context,
            if (granted) "Notifications enabled" else "Notification permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(onBack = onBack)
        Box(Modifier.padding(18.dp)) { SectionTitle("Settings") }
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            SettingsRow("Notifications", notificationsEnabled) { turnOn ->
                if (turnOn) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsEnabled = true
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.revokeSelfPermissionOnKill(Manifest.permission.POST_NOTIFICATIONS)
                        Toast.makeText(
                            context,
                            "Notifications will be turned off when you close the app",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    notificationsEnabled = false
                }
            }
            HorizontalDivider(color = DividerColor)
            Text("About Dishly", modifier = Modifier.padding(vertical = 16.dp), fontSize = 16.sp)
        }
    }
}

@Composable
private fun SettingsRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    val user = state.user

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        DishlyTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionTitle("My Profile")
            Icon(
                Icons.Default.Person,
                null,
                tint = PurplePrimary,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ChipBg)
                    .padding(28.dp)
            )
            Text(
                user.username,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
            DishlyPrimaryButton(
                text = "Edit Profile",
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            )
            DishlyPrimaryButton(
                text = "Log out",
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                backgroundColor = PurplePrimary
            )
        }
    }
}
