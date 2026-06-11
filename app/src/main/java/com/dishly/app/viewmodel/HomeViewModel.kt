package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val popularRecipes: List<Recipe> = emptyList(),
    val latestRecipes: List<Recipe> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = HomeUiState(
            popularRecipes = RecipeRepository.popularRecipes(),
            latestRecipes = RecipeRepository.latestRecipes()
        )
    }
}
