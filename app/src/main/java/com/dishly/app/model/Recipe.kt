package com.dishly.app.model

/**
 * Model (MVVM domain) — core entity: a recipe.
 *
 * @property imageRes drawable used as the recipe "photo" (colored placeholder,
 *  since the app runs offline, without network/backend).
 * @property comments mutable list of comments shown on the Recipe Page.
 */
data class Recipe(
    val id: Int,
    val title: String,
    val rating: Int,
    val durationMin: Int,
    val difficulty: String,
    val imageRes: Int,
    val description: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val tags: List<String> = emptyList(),
    val comments: MutableList<Comment> = mutableListOf(),
    var isFavorite: Boolean = false,
    val isPopular: Boolean = false,
    val isLatest: Boolean = false
)
