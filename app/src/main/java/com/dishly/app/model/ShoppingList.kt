package com.dishly.app.model

data class ShoppingListItem(
    val id: String,
    val name: String,
    val measure: String = "",
    val checked: Boolean = false,
    val recipeId: Int? = null
) {
    fun display(): String =
        if (measure.isBlank()) name else "$measure $name"
}

data class ShoppingList(
    val recipeId: Int,
    val recipeTitle: String,
    val items: List<ShoppingListItem> = emptyList()
)

object IngredientMatcher {
    fun namesMatch(a: String, b: String): Boolean {
        val left = a.trim().lowercase()
        val right = b.trim().lowercase()
        if (left.isEmpty() || right.isEmpty()) return false
        val leftNorm = left.replace(" ", "_")
        val rightNorm = right.replace(" ", "_")
        return left == right ||
            leftNorm == rightNorm ||
            left.contains(right) ||
            right.contains(left) ||
            leftNorm.contains(rightNorm) ||
            rightNorm.contains(leftNorm)
    }

    fun isInPantry(ingredient: Ingredient, pantryNames: Set<String>): Boolean {
        val foodName = ingredient.matchingName()
        if (foodName.isBlank()) return false
        return pantryNames.any { pantry ->
            namesMatch(foodName, IngredientNormalizer.cleanName(pantry).ifBlank { pantry })
        }
    }

    fun missingIngredients(
        recipeIngredients: List<Ingredient>,
        pantryNames: Set<String>
    ): List<Ingredient> =
        recipeIngredients
            .map { IngredientNormalizer.normalize(it) }
            .filter { it.name.isNotBlank() }
            .filterNot { isInPantry(it, pantryNames) }
            // One line per food name (keep first measure seen)
            .distinctBy { it.matchingName() }

    @Suppress("UNUSED_PARAMETER")
    fun itemId(name: String, measure: String = ""): String {
        val base = IngredientNormalizer.cleanName(name)
            .ifBlank { name }
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
        return base.ifEmpty { "item" }
    }
}
