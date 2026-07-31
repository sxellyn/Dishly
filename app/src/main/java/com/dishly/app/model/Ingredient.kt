package com.dishly.app.model

data class Ingredient(
    val name: String,
    val measure: String = ""
) {
    fun display(): String =
        if (measure.isBlank()) name else "$measure $name"
}

/**
 * Recipe ingredients as structured items. Falls back to display strings
 * (used by local mock recipes) when [Recipe.ingredientItems] is empty.
 * Names are normalized so shopping lists show food names, not tsp/cups/chopped.
 */
fun Recipe.resolvedIngredients(): List<Ingredient> =
    ingredientItems.ifEmpty {
        ingredients.map { raw ->
            Ingredient(name = raw.trim(), measure = "")
        }
    }.map { IngredientNormalizer.normalize(it) }

/** Clean food name used for pantry matching (ignores tsp/cups/chopped/etc.). */
fun Ingredient.matchingName(): String =
    IngredientNormalizer.cleanName(name).ifBlank { name }.lowercase()
