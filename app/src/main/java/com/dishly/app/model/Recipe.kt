package com.dishly.app.model

import com.dishly.app.R

data class Recipe(
    val id: Int,
    val title: String,
    val rating: Int,
    val durationMin: Int,
    val difficulty: String,
    val imageRes: Int = R.drawable.foodie,
    val imageUrl: String? = null,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val tags: List<String> = emptyList(),
    val comments: MutableList<Comment> = mutableListOf(),
    var isFavorite: Boolean = false,
    val isPopular: Boolean = false,
    val isLatest: Boolean = false,
    val isLoading: Boolean = false
) {
    companion object {
        val LOADING = Recipe(
            id = -1,
            title = "",
            rating = 0,
            durationMin = 0,
            difficulty = "—",
            description = "",
            ingredients = emptyList(),
            steps = emptyList(),
            isLoading = true
        )
    }
}
