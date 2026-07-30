package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dishly.app.api.MealService
import com.dishly.app.api.toRecipe
import com.dishly.app.data.FavoritesRepository
import com.dishly.app.data.RecipeRepository
import com.dishly.app.data.RecentRecipesRepository
import com.dishly.app.data.RecipeStatsRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val commentText: String = "",
    val message: String? = null
)

class RecipeDetailViewModel(
    private val recipeId: Int,
    private val service: MealService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, recipe = null)
        service.lookup(recipeId.toString()) { meal ->
            viewModelScope.launch {
                val base = meal?.toRecipe() ?: RecipeRepository.recipeById(recipeId)
                if (base == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                val isFavorite = FavoritesRepository.isFavorite(recipeId)
                val favoriteCount = RecipeStatsRepository.getFavoriteCount(recipeId)
                val recipe = base.copy(isFavorite = isFavorite, rating = favoriteCount)
                _uiState.value = _uiState.value.copy(recipe = recipe, isLoading = false)
                RecentRecipesRepository.addRecentRecipe(recipeId)
            }
        }
    }

    fun onCommentTextChange(text: String) {
        _uiState.value = _uiState.value.copy(commentText = text)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _uiState.value.recipe ?: return@launch
            if (current.isFavorite) {
                FavoritesRepository.removeFavorite(recipeId)
                RecipeStatsRepository.decrementFavoriteCount(recipeId)
                val newCount = RecipeStatsRepository.getFavoriteCount(recipeId).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(
                    recipe = current.copy(isFavorite = false, rating = newCount),
                    message = "Removed from favorites"
                )
            } else {
                FavoritesRepository.addFavorite(recipeId)
                RecipeStatsRepository.incrementFavoriteCount(recipeId)
                val newCount = RecipeStatsRepository.getFavoriteCount(recipeId)
                _uiState.value = _uiState.value.copy(
                    recipe = current.copy(isFavorite = true, rating = newCount),
                    message = "Added to favorites"
                )
            }
        }
    }

    fun sendComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "Write a comment first")
            return
        }
        RecipeRepository.addComment(recipeId, text)
        val comments = RecipeRepository.recipeById(recipeId)?.comments
        _uiState.value = _uiState.value.copy(
            commentText = "",
            recipe = _uiState.value.recipe?.copy(comments = comments ?: mutableListOf()),
            message = "Comment added!"
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(
        private val recipeId: Int,
        private val service: MealService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeDetailViewModel(recipeId, service) as T
        }
    }
}
