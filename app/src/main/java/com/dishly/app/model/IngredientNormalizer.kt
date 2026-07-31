package com.dishly.app.model

/**
 * Cleans TheMealDB-style ingredient strings so shopping lists show the food name
 * (e.g. "Carrots") instead of units/prep noise ("tsp", "chopped", "cups").
 */
object IngredientNormalizer {

    private val UNIT_WORDS = setOf(
        "tsp", "tsps", "teaspoon", "teaspoons",
        "tbsp", "tbsps", "tablespoon", "tablespoons",
        "cup", "cups", "c",
        "g", "gram", "grams", "kg", "kilogram", "kilograms",
        "ml", "milliliter", "milliliters", "l", "liter", "liters", "litre", "litres",
        "oz", "ounce", "ounces", "lb", "lbs", "pound", "pounds",
        "pinch", "pinches", "dash", "dashes",
        "clove", "cloves", "slice", "slices",
        "piece", "pieces", "can", "cans", "tin", "tins",
        "pkg", "package", "packages", "packet", "packets",
        "bunch", "bunches", "handful", "handfuls",
        "quart", "quarts", "pint", "pints", "gallon", "gallons",
        "stick", "sticks", "drop", "drops",
        "tb", "tbs", "t", // common shorthand
        "floz", "fl"
    )

    private val PREP_WORDS = setOf(
        "chopped", "diced", "minced", "sliced", "crushed", "grated",
        "peeled", "fresh", "dried", "ground", "shredded", "julienned",
        "large", "small", "medium", "whole", "halved", "quartered",
        "optional", "finely", "roughly", "thinly", "thickly",
        "softened", "melted", "beaten", "cooked", "raw", "frozen",
        "canned", "drained", "rinsed", "seeded", "boneless", "skinless",
        "ripe", "extra", "virgin", "hot", "cold", "warm",
        "to", "taste", "for", "serving", "servings", "garnish",
        "of", "and", "or", "a", "an", "the", "as", "needed",
        "plus", "more", "about", "approx", "approximately"
    )

    private val QUANTITY_REGEX = Regex(
        """^(?:
            \d+\s*/\s*\d+|
            \d+[.,]\d+|
            \d+|
            [½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]
        )+""",
        RegexOption.COMMENTS
    )

    private val NOISE_WORDS = UNIT_WORDS + PREP_WORDS

    fun normalize(ingredient: Ingredient): Ingredient {
        val rawName = ingredient.name.trim()
        val rawMeasure = ingredient.measure.trim()

        // API sometimes puts the food in measure and unit/prep in name (or vice-versa).
        val (candidateName, candidateMeasure) = when {
            rawName.isNotEmpty() && isMostlyNoise(rawName) &&
                rawMeasure.isNotEmpty() && !isMostlyNoise(rawMeasure) ->
                rawMeasure to rawName
            rawName.isEmpty() && rawMeasure.isNotEmpty() ->
                rawMeasure to ""
            else ->
                rawName to rawMeasure
        }

        val foodName = extractFoodName(candidateName).ifBlank {
            extractFoodName("$candidateMeasure $candidateName")
        }.ifBlank {
            // Last resort: keep original non-noise text.
            listOf(rawName, rawMeasure)
                .firstOrNull { it.isNotBlank() && !isMostlyNoise(it) }
                .orEmpty()
        }.ifBlank { rawName.ifBlank { rawMeasure } }

        val measure = when {
            candidateMeasure.isNotBlank() && !looksLikeFoodOnly(candidateMeasure) ->
                candidateMeasure
            else ->
                extractLeadingMeasure(ingredient.display()).ifBlank { candidateMeasure }
        }

        return Ingredient(
            name = foodName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            measure = measure.trim()
        )
    }

    fun cleanName(raw: String): String = extractFoodName(raw)

    private fun extractFoodName(raw: String): String {
        var text = raw.trim()
        if (text.isEmpty()) return ""

        // Drop trailing prep notes: "Carrots, chopped" / "Carrots - chopped"
        text = text.split(",", limit = 2).first().trim()
        text = text.split(" - ", limit = 2).first().trim()
        text = text.split(" – ", limit = 2).first().trim()

        // Remove repeated leading quantities + units: "1 tsp", "2 cups", "1/2"
        var previous: String
        do {
            previous = text
            text = text.replace(QUANTITY_REGEX, "").trim()
            text = stripLeadingNoiseWords(text)
        } while (text != previous && text.isNotEmpty())

        // Remove leftover noise tokens anywhere that are pure unit/prep words
        val tokens = text.split(Regex("\\s+")).filter { token ->
            val key = normalizeToken(token)
            key.isNotEmpty() && key !in NOISE_WORDS
        }

        return tokens.joinToString(" ").trim()
    }

    private fun extractLeadingMeasure(raw: String): String {
        val tokens = raw.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""

        val measureTokens = mutableListOf<String>()
        for (token in tokens) {
            val key = normalizeToken(token)
            val isQuantity = QUANTITY_REGEX.matches(token) ||
                token.any { it.isDigit() } ||
                key in UNIT_WORDS
            if (isQuantity || key in PREP_WORDS && measureTokens.isNotEmpty()) {
                measureTokens += token
            } else {
                break
            }
        }
        return measureTokens.joinToString(" ").trim()
    }

    private fun stripLeadingNoiseWords(text: String): String {
        val tokens = text.split(Regex("\\s+")).toMutableList()
        while (tokens.isNotEmpty() && normalizeToken(tokens.first()) in NOISE_WORDS) {
            tokens.removeAt(0)
        }
        return tokens.joinToString(" ").trim()
    }

    private fun isMostlyNoise(text: String): Boolean {
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return true
        val meaningful = tokens.count { token ->
            val key = normalizeToken(token)
            key.isNotEmpty() &&
                key !in NOISE_WORDS &&
                !QUANTITY_REGEX.matches(token) &&
                !token.any { it.isDigit() }
        }
        return meaningful == 0
    }

    private fun looksLikeFoodOnly(text: String): Boolean {
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return false
        return tokens.all { token ->
            val key = normalizeToken(token)
            key.isNotEmpty() &&
                key !in UNIT_WORDS &&
                !QUANTITY_REGEX.matches(token) &&
                !token.any { it.isDigit() }
        }
    }

    private fun normalizeToken(token: String): String =
        token.lowercase()
            .replace(Regex("[^a-z]"), "")
}
