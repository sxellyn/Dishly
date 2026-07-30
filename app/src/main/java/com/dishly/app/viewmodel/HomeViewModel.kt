package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dishly.app.api.MealService
import com.dishly.app.api.toRecipe
import com.dishly.app.data.RecentRecipesRepository
import com.dishly.app.data.RecipeStatsRepository
import com.dishly.app.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val popularRecipes: List<Recipe> = emptyList(),
    val latestRecipes: List<Recipe> = emptyList(),
    val showLatestSection: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val service: MealService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadPopular() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            popularRecipes = loadingPlaceholders(4)
        )
        viewModelScope.launch {
            val topFavorited = RecipeStatsRepository.getTopFavoritedRecipes(limit = 4)
            val favoritedIds = topFavorited.map { it.first }
            val countById = topFavorited.associate { it.first to it.second }
            loadFavoritedThenRandom(favoritedIds, countById)
        }
    }

    private fun loadFavoritedThenRandom(
        favoritedIds: List<Int>,
        countById: Map<Int, Int>
    ) {
        if (favoritedIds.isEmpty()) {
            fetchRandomPopular(4, emptySet()) { randomRecipes ->
                finishPopularLoad(emptyList(), randomRecipes)
            }
            return
        }
        service.lookupMeals(favoritedIds.map { it.toString() }) { meals ->
            viewModelScope.launch {
                val mealById = meals.associateBy { it.idMeal?.toIntOrNull() }
                val favoritedRecipes = favoritedIds.mapNotNull { id ->
                    mealById[id]?.toRecipe()?.copy(
                        isPopular = true,
                        rating = countById[id] ?: 0
                    )
                }
                val stillNeedRandom = 4 - favoritedRecipes.size
                if (stillNeedRandom <= 0) {
                    finishPopularLoad(favoritedRecipes, emptyList())
                    return@launch
                }
                val excludeIds = favoritedRecipes.map { it.id }.toSet()
                fetchRandomPopular(stillNeedRandom, excludeIds) { randomRecipes ->
                    finishPopularLoad(favoritedRecipes, randomRecipes)
                }
            }
        }
    }

    private fun fetchRandomPopular(
        count: Int,
        excludeIds: Set<Int>,
        onResult: (List<Recipe>) -> Unit
    ) {
        val collected = mutableListOf<Recipe>()
        val seen = excludeIds.toMutableSet()
        var attempts = 0

        fun fetchMore() {
            if (collected.size >= count || attempts >= 6) {
                viewModelScope.launch {
                    val enriched = enrichWithFavoriteCounts(
                        collected.take(count).map { it.copy(isPopular = true) }
                    )
                    onResult(enriched)
                }
                return
            }
            attempts++
            val remaining = count - collected.size
            service.getRandomMeals(remaining) { meals ->
                viewModelScope.launch {
                    meals.forEach { meal ->
                        val recipe = meal.toRecipe()
                        if (recipe.id !in seen) {
                            seen.add(recipe.id)
                            collected.add(recipe)
                        }
                    }
                    fetchMore()
                }
            }
        }
        fetchMore()
    }

    private fun finishPopularLoad(favoritedRecipes: List<Recipe>, randomRecipes: List<Recipe>) {
        val combined = favoritedRecipes + randomRecipes
        if (combined.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                popularRecipes = emptyList(),
                error = "Could not load recipes. Check your connection."
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                popularRecipes = combined
            )
        }
    }

    fun loadRecent() {
        viewModelScope.launch {
            val recentIds = RecentRecipesRepository.getRecentRecipeIds()
            if (recentIds.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    showLatestSection = false,
                    latestRecipes = emptyList()
                )
                return@launch
            }
            val placeholders = loadingPlaceholders(recentIds.size)
            _uiState.value = _uiState.value.copy(
                showLatestSection = true,
                latestRecipes = placeholders
            )
            service.lookupMeals(recentIds) { meals ->
                viewModelScope.launch {
                    val recipes = meals.map { it.toRecipe().copy(isLatest = true) }
                    val enriched = enrichWithFavoriteCounts(recipes)
                    _uiState.value = _uiState.value.copy(latestRecipes = enriched)
                }
            }
        }
    }

    private suspend fun enrichWithFavoriteCounts(recipes: List<Recipe>): List<Recipe> {
        val counts = RecipeStatsRepository.getFavoriteCounts(recipes.map { it.id })
        return recipes.map { recipe -> recipe.copy(rating = counts[recipe.id] ?: 0) }
    }

    private fun loadingPlaceholders(count: Int): List<Recipe> =
        List(count) { index -> Recipe.LOADING.copy(id = -1 - index) }
}

class HomeViewModelFactory(
    private val service: MealService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
