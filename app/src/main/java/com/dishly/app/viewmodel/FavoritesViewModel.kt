package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dishly.app.api.MealService
import com.dishly.app.api.toRecipe
import com.dishly.app.data.FavoritesRepository
import com.dishly.app.data.RecipeStatsRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Recipe> = emptyList(),
    val isLoading: Boolean = false
)

class FavoritesViewModel(
    private val service: MealService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val ids = FavoritesRepository.getFavoriteIds()
            if (ids.isEmpty()) {
                _uiState.value = FavoritesUiState(isLoading = false, favorites = emptyList())
                return@launch
            }
            _uiState.value = FavoritesUiState(
                isLoading = true,
                favorites = List(ids.size) { index -> Recipe.LOADING.copy(id = -1 - index) }
            )
            service.lookupMeals(ids) { meals ->
                viewModelScope.launch {
                    val recipes = meals.map { it.toRecipe().copy(isFavorite = true) }
                    val counts = RecipeStatsRepository.getFavoriteCounts(recipes.map { it.id })
                    val enriched = recipes.map { recipe ->
                        recipe.copy(rating = counts[recipe.id] ?: 0)
                    }
                    _uiState.value = FavoritesUiState(isLoading = false, favorites = enriched)
                }
            }
        }
    }
}

class FavoritesViewModelFactory(
    private val service: MealService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
