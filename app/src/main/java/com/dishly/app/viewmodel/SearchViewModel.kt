package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchUiState(
    val ingredientNames: List<String> = emptyList(),
    val selectedIngredientNames: Set<String> = emptySet(),
    val emptyFridgeMode: Boolean = false,
    val showResults: Boolean = false,
    val results: List<Recipe> = emptyList()
)

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(
            ingredientNames = RecipeRepository.pickerIngredients
        )
    }

    /** Visual toggle only — pink chip when selected (API wiring later). */
    fun toggleIngredient(name: String) {
        val selected = _uiState.value.selectedIngredientNames.toMutableSet()
        if (selected.contains(name)) selected.remove(name) else selected.add(name)
        _uiState.value = _uiState.value.copy(selectedIngredientNames = selected)
    }

    /** Visual toggle only — pink button when active (API wiring later). */
    fun toggleEmptyFridge() {
        _uiState.value = _uiState.value.copy(
            emptyFridgeMode = !_uiState.value.emptyFridgeMode
        )
    }

    fun search() {
        _uiState.value = _uiState.value.copy(
            showResults = true,
            results = RecipeRepository.recipes.take(6)
        )
    }

    fun backToPicker() {
        _uiState.value = _uiState.value.copy(showResults = false)
    }
}
