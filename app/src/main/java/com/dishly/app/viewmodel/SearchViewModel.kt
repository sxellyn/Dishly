package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dishly.app.api.MealService
import com.dishly.app.api.toRecipe
import com.dishly.app.data.PantryRepository
import com.dishly.app.data.RecipeRepository
import com.dishly.app.data.RecipeStatsRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val ingredientNames: List<String> = emptyList(),
    val searchQuery: String = "",
    val ingredientSuggestions: List<String> = emptyList(),
    val selectedIngredientNames: Set<String> = emptySet(),
    val emptyFridgeMode: Boolean = false,
    val showResults: Boolean = false,
    val results: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val service: MealService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val pantry = PantryRepository.getIngredientNames()
            if (pantry.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(selectedIngredientNames = pantry)
            }
        }
        service.ingredientList { ingredients ->
            val names = ingredients
                .mapNotNull { it.strIngredient }
                .filter { it.isNotBlank() }
                .sorted()
            _uiState.value = _uiState.value.copy(
                ingredientNames = names.ifEmpty { RecipeRepository.pickerIngredients }
            )
        }
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
        viewModelScope.launch { PantryRepository.addIngredient(name) }
    }

    fun removeSelectedIngredient(name: String) {
        val selected = _uiState.value.selectedIngredientNames - name
        val suggestions = buildSuggestions(
            _uiState.value.searchQuery,
            _uiState.value.copy(selectedIngredientNames = selected)
        )
        _uiState.value = _uiState.value.copy(
            selectedIngredientNames = selected,
            ingredientSuggestions = suggestions
        )
        viewModelScope.launch { PantryRepository.removeIngredient(name) }
    }

    fun toggleEmptyFridge() {
        _uiState.value = _uiState.value.copy(
            emptyFridgeMode = !_uiState.value.emptyFridgeMode
        )
    }

    fun search() {
        val ingredients = _uiState.value.selectedIngredientNames.toList()
        if (ingredients.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Select at least one ingredient")
            return
        }
        viewModelScope.launch {
            PantryRepository.setIngredients(ingredients.toSet())
        }
        _uiState.value = _uiState.value.copy(
            showResults = true,
            isLoading = true,
            error = null,
            results = List(5) { index -> Recipe.LOADING.copy(id = -1 - index) }
        )
        service.filterByIngredientsIntersection(
            ingredients = ingredients,
            preferFewestIngredients = _uiState.value.emptyFridgeMode
        ) { meals ->
            viewModelScope.launch {
                if (meals.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = emptyList(),
                        error = "No recipes found with all selected ingredients."
                    )
                    return@launch
                }
                val recipes = meals.map { it.toRecipe() }
                val counts = RecipeStatsRepository.getFavoriteCounts(recipes.map { it.id })
                val enriched = recipes.map { recipe ->
                    recipe.copy(rating = counts[recipe.id] ?: 0)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = enriched
                )
            }
        }
    }

    fun backToPicker() {
        _uiState.value = _uiState.value.copy(showResults = false, error = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun buildSuggestions(query: String, state: SearchUiState): List<String> {
        if (query.isBlank()) return emptyList()
        val prefix = query.lowercase()
        return state.ingredientNames.filter { name ->
            name.lowercase().startsWith(prefix) && !state.selectedIngredientNames.contains(name)
        }.take(8)
    }
}

class SearchViewModelFactory(
    private val service: MealService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
