package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FavoritesUiState(val favorites: List<Recipe> = emptyList())

class FavoritesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = FavoritesUiState(RecipeRepository.favoriteRecipes())
    }
}
