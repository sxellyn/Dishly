package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dishly.app.api.MealService
import com.dishly.app.api.toRecipe
import com.dishly.app.data.PantryRepository
import com.dishly.app.data.RecipeRepository
import com.dishly.app.data.ShoppingListRepository
import com.dishly.app.model.IngredientMatcher
import com.dishly.app.model.ShoppingList
import com.dishly.app.model.ShoppingListItem
import com.dishly.app.model.resolvedIngredients
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val recipeTitle: String = "",
    val items: List<ShoppingListItem> = emptyList(),
    val pantryCount: Int = 0,
    val coveredCount: Int = 0,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ShoppingListViewModel(
    private val recipeId: Int,
    private val service: MealService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState(isLoading = true))
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        service.lookup(recipeId.toString()) { meal ->
            viewModelScope.launch {
                val recipe = meal?.toRecipe() ?: RecipeRepository.recipeById(recipeId)
                if (recipe == null) {
                    _uiState.value = ShoppingListUiState(
                        isLoading = false,
                        error = "Recipe not found"
                    )
                    return@launch
                }

                val pantry = PantryRepository.getIngredientNames()
                val recipeIngredients = recipe.resolvedIngredients()
                val missing = IngredientMatcher.missingIngredients(recipeIngredients, pantry)
                val covered = recipeIngredients.size - missing.size

                val previous = ShoppingListRepository.getActiveList()
                val previousChecks = if (previous?.recipeId == recipeId) {
                    previous.items.associate { it.id to it.checked }
                } else {
                    emptyMap()
                }

                val items = missing.map { ingredient ->
                    val id = IngredientMatcher.itemId(ingredient.name, ingredient.measure)
                    ShoppingListItem(
                        id = id,
                        name = ingredient.name,
                        measure = ingredient.measure,
                        checked = previousChecks[id] ?: false,
                        recipeId = recipeId
                    )
                }

                val list = ShoppingList(
                    recipeId = recipeId,
                    recipeTitle = recipe.title,
                    items = items
                )
                ShoppingListRepository.saveActiveList(list)

                _uiState.value = ShoppingListUiState(
                    recipeTitle = recipe.title,
                    items = items,
                    pantryCount = pantry.size,
                    coveredCount = covered.coerceAtLeast(0),
                    isLoading = false,
                    isEmpty = items.isEmpty(),
                    message = when {
                        items.isEmpty() && recipeIngredients.isNotEmpty() ->
                            "You already have all ingredients for this recipe!"
                        pantry.isEmpty() ->
                            "Pantry is empty — showing all recipe ingredients. Select what you have in Search to refine this list."
                        else -> null
                    }
                )
            }
        }
    }

    fun toggleChecked(itemId: String) {
        viewModelScope.launch {
            val current = _uiState.value.items
            val item = current.find { it.id == itemId } ?: return@launch
            val checked = !item.checked
            val updated = current.map {
                if (it.id == itemId) it.copy(checked = checked) else it
            }
            _uiState.value = _uiState.value.copy(items = updated)
            ShoppingListRepository.toggleChecked(itemId, checked)
        }
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
            return ShoppingListViewModel(recipeId, service) as T
        }
    }
}
