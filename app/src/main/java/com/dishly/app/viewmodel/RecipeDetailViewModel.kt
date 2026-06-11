package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.Comment
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val commentText: String = "",
    val message: String? = null
)

class RecipeDetailViewModel(private val recipeId: Int) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(recipe = RecipeRepository.recipeById(recipeId))
    }

    fun onCommentTextChange(text: String) {
        _uiState.value = _uiState.value.copy(commentText = text)
    }

    fun toggleFavorite() {
        RecipeRepository.toggleFavorite(recipeId)
        load()
        val isFav = RecipeRepository.recipeById(recipeId)?.isFavorite == true
        _uiState.value = _uiState.value.copy(
            message = if (isFav) "Added to favorites" else "Removed from favorites"
        )
    }

    fun sendComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "Write a comment first")
            return
        }
        RecipeRepository.addComment(recipeId, text)
        _uiState.value = _uiState.value.copy(
            commentText = "",
            recipe = RecipeRepository.recipeById(recipeId),
            message = "Comment added!"
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(private val recipeId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeDetailViewModel(recipeId) as T
        }
    }
}
