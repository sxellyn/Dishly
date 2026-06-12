package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchUiState(
    val ingredientNames: List<String> = emptyList(),
    val searchQuery: String = "",
    val ingredientSuggestions: List<String> = emptyList(),
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

    fun onSearchQueryChange(query: String) {
        val suggestions = buildSuggestions(query, _uiState.value)
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            ingredientSuggestions = suggestions
        )
    }

    fun selectIngredient(name: String) {
        val selected = _uiState.value.selectedIngredientNames + name
        _uiState.value = _uiState.value.copy(
            selectedIngredientNames = selected,
            searchQuery = "",
            ingredientSuggestions = emptyList()
        )
    }

    fun removeSelectedIngredient(name: String) {
        val selected = _uiState.value.selectedIngredientNames - name
        val suggestions = buildSuggestions(_uiState.value.searchQuery, _uiState.value.copy(selectedIngredientNames = selected))
        _uiState.value = _uiState.value.copy(
            selectedIngredientNames = selected,
            ingredientSuggestions = suggestions
        )
    }

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

    private fun buildSuggestions(query: String, state: SearchUiState): List<String> {
        if (query.isBlank()) return emptyList()
        val prefix = query.lowercase()
        return state.ingredientNames.filter { name ->
            name.lowercase().startsWith(prefix) && !state.selectedIngredientNames.contains(name)
        }
    }
}
