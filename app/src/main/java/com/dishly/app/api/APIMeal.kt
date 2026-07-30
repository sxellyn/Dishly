package com.dishly.app.api

import com.dishly.app.model.Recipe

data class APIMealsResponse(
    var meals: List<APIMeal>? = null
)

data class APIMeal(
    var idMeal: String? = null,
    var strMeal: String? = null,
    var strMealThumb: String? = null,
    var strCategory: String? = null,
    var strArea: String? = null,
    var strInstructions: String? = null,
    var strTags: String? = null,
    var strIngredient1: String? = null,
    var strIngredient2: String? = null,
    var strIngredient3: String? = null,
    var strIngredient4: String? = null,
    var strIngredient5: String? = null,
    var strIngredient6: String? = null,
    var strIngredient7: String? = null,
    var strIngredient8: String? = null,
    var strIngredient9: String? = null,
    var strIngredient10: String? = null,
    var strIngredient11: String? = null,
    var strIngredient12: String? = null,
    var strIngredient13: String? = null,
    var strIngredient14: String? = null,
    var strIngredient15: String? = null,
    var strIngredient16: String? = null,
    var strIngredient17: String? = null,
    var strIngredient18: String? = null,
    var strIngredient19: String? = null,
    var strIngredient20: String? = null,
    var strMeasure1: String? = null,
    var strMeasure2: String? = null,
    var strMeasure3: String? = null,
    var strMeasure4: String? = null,
    var strMeasure5: String? = null,
    var strMeasure6: String? = null,
    var strMeasure7: String? = null,
    var strMeasure8: String? = null,
    var strMeasure9: String? = null,
    var strMeasure10: String? = null,
    var strMeasure11: String? = null,
    var strMeasure12: String? = null,
    var strMeasure13: String? = null,
    var strMeasure14: String? = null,
    var strMeasure15: String? = null,
    var strMeasure16: String? = null,
    var strMeasure17: String? = null,
    var strMeasure18: String? = null,
    var strMeasure19: String? = null,
    var strMeasure20: String? = null,
    var idIngredient: String? = null,
    var strIngredient: String? = null,
    var strDescription: String? = null,
    var strThumb: String? = null,
    var strType: String? = null
)

fun APIMeal.toRecipe(): Recipe {
    val ingredients = collectIngredients()
    val steps = strInstructions
        ?.split("\r\n", "\n")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    return Recipe(
        id = idMeal?.toIntOrNull() ?: 0,
        title = strMeal ?: strIngredient ?: "...",
        rating = 0,
        durationMin = 0,
        difficulty = "—",
        imageUrl = strMealThumb ?: strThumb,
        description = strDescription ?: strInstructions.orEmpty(),
        ingredients = ingredients,
        steps = if (steps.isEmpty() && !strInstructions.isNullOrBlank()) {
            listOf(strInstructions!!)
        } else {
            steps
        },
        tags = strTags?.split(",")?.map { it.trim() }.orEmpty().filter { it.isNotEmpty() }
    )
}

fun APIMealsResponse.toRecipes(): List<Recipe> =
    meals?.map { it.toRecipe() }.orEmpty()

fun APIMeal.allIngredientNames(): List<String> =
    (1..20).mapNotNull { index -> ingredientAt(index)?.trim() }.filter { it.isNotEmpty() }

fun APIMeal.containsIngredient(search: String): Boolean {
    val needle = search.trim().lowercase()
    if (needle.isEmpty()) return false
    val needleNorm = needle.replace(" ", "_")
    return allIngredientNames().any { ingredient ->
        val lower = ingredient.lowercase()
        val norm = lower.replace(" ", "_")
        lower == needle ||
            norm == needleNorm ||
            lower.contains(needle) ||
            needle.contains(lower) ||
            norm.contains(needleNorm) ||
            needleNorm.contains(norm)
    }
}

fun APIMeal.containsAllIngredients(searches: List<String>): Boolean =
    searches.all { containsIngredient(it) }

private fun APIMeal.collectIngredients(): List<String> {
    return (1..20).mapNotNull { index ->
        val ingredient = ingredientAt(index)?.trim().orEmpty()
        if (ingredient.isEmpty()) return@mapNotNull null
        val measure = measureAt(index)?.trim().orEmpty()
        if (measure.isEmpty()) ingredient else "$measure $ingredient"
    }
}

private fun APIMeal.ingredientAt(index: Int): String? = when (index) {
    1 -> strIngredient1
    2 -> strIngredient2
    3 -> strIngredient3
    4 -> strIngredient4
    5 -> strIngredient5
    6 -> strIngredient6
    7 -> strIngredient7
    8 -> strIngredient8
    9 -> strIngredient9
    10 -> strIngredient10
    11 -> strIngredient11
    12 -> strIngredient12
    13 -> strIngredient13
    14 -> strIngredient14
    15 -> strIngredient15
    16 -> strIngredient16
    17 -> strIngredient17
    18 -> strIngredient18
    19 -> strIngredient19
    20 -> strIngredient20
    else -> null
}

private fun APIMeal.measureAt(index: Int): String? = when (index) {
    1 -> strMeasure1
    2 -> strMeasure2
    3 -> strMeasure3
    4 -> strMeasure4
    5 -> strMeasure5
    6 -> strMeasure6
    7 -> strMeasure7
    8 -> strMeasure8
    9 -> strMeasure9
    10 -> strMeasure10
    11 -> strMeasure11
    12 -> strMeasure12
    13 -> strMeasure13
    14 -> strMeasure14
    15 -> strMeasure15
    16 -> strMeasure16
    17 -> strMeasure17
    18 -> strMeasure18
    19 -> strMeasure19
    20 -> strMeasure20
    else -> null
}
