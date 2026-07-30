package com.dishly.app.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MealService {

    private val mealAPI: MealServiceAPI
    private val maxSearchResults = 5
    private val maxCandidateLookups = 40

    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(MealServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        mealAPI = retrofitAPI.create(MealServiceAPI::class.java)
    }

    private fun <T> enqueue(call: Call<T?>, onResponse: ((T?) -> Unit)? = null) {
        call.enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                onResponse?.invoke(response.body())
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                Log.w("Dishly WARNING", t.message ?: "Meal API request failed")
                onResponse?.invoke(null)
            }
        })
    }

    fun getRandom(onResponse: (APIMeal?) -> Unit) {
        enqueue(mealAPI.random()) { response ->
            onResponse(response?.meals?.firstOrNull())
        }
    }

    fun getRandomMeals(count: Int, onResponse: (List<APIMeal>) -> Unit) {
        val results = mutableListOf<APIMeal>()
        if (count <= 0) {
            onResponse(results)
            return
        }
        fetchRandomBatch(count, results, onResponse)
    }

    private fun fetchRandomBatch(
        remaining: Int,
        results: MutableList<APIMeal>,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        enqueue(mealAPI.random()) { response ->
            val meal = response?.meals?.firstOrNull()
            if (meal != null) {
                results.add(meal)
            }
            if (remaining <= 1) {
                onResponse(results)
            } else {
                fetchRandomBatch(remaining - 1, results, onResponse)
            }
        }
    }

    fun lookup(id: String, onResponse: (APIMeal?) -> Unit) {
        enqueue(mealAPI.lookup(id)) { response ->
            onResponse(response?.meals?.firstOrNull())
        }
    }

    fun lookupMeals(ids: List<String>, onResponse: (List<APIMeal>) -> Unit) {
        if (ids.isEmpty()) {
            onResponse(emptyList())
            return
        }
        lookupBatch(ids, mutableListOf(), onResponse)
    }

    private fun lookupBatch(
        remainingIds: List<String>,
        results: MutableList<APIMeal>,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        if (remainingIds.isEmpty()) {
            onResponse(results)
            return
        }
        lookup(remainingIds.first()) { meal ->
            if (meal != null) {
                results.add(meal)
            }
            lookupBatch(remainingIds.drop(1), results, onResponse)
        }
    }

    fun search(name: String, onResponse: (List<APIMeal>) -> Unit) {
        enqueue(mealAPI.search(name)) { response ->
            onResponse(response?.meals.orEmpty())
        }
    }

    fun filterByIngredient(ingredient: String, onResponse: (List<APIMeal>) -> Unit) {
        enqueue(mealAPI.filterByIngredient(normalizeIngredient(ingredient))) { response ->
            onResponse(response?.meals.orEmpty())
        }
    }

    fun filterByIngredientsIntersection(
        ingredients: List<String>,
        preferFewestIngredients: Boolean = false,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        val selected = ingredients.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (selected.isEmpty()) {
            onResponse(emptyList())
            return
        }
        collectFilterCandidates(selected, mutableSetOf(), 0) { candidateIds ->
            if (candidateIds.isEmpty()) {
                onResponse(emptyList())
                return@collectFilterCandidates
            }
            val candidates = candidateIds.take(maxCandidateLookups).toList()
            if (preferFewestIngredients) {
                collectAllMatches(candidates, selected, mutableListOf()) { matches ->
                    onResponse(
                        matches
                            .sortedBy { it.allIngredientNames().size }
                            .take(maxSearchResults)
                    )
                }
            } else {
                lookupUntilMatchLimit(candidates, selected, onResponse)
            }
        }
    }

    private fun lookupUntilMatchLimit(
        candidateIds: List<String>,
        selected: List<String>,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        if (candidateIds.isEmpty()) {
            onResponse(emptyList())
            return
        }
        val matches = mutableListOf<APIMeal>()
        lookupNextMatch(candidateIds, selected, matches, onResponse)
    }

    private fun lookupNextMatch(
        remainingIds: List<String>,
        selected: List<String>,
        matches: MutableList<APIMeal>,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        if (matches.size >= maxSearchResults || remainingIds.isEmpty()) {
            onResponse(matches)
            return
        }
        lookup(remainingIds.first()) { meal ->
            if (meal != null && meal.containsAllIngredients(selected)) {
                matches.add(meal)
            }
            lookupNextMatch(remainingIds.drop(1), selected, matches, onResponse)
        }
    }

    private fun collectAllMatches(
        remainingIds: List<String>,
        selected: List<String>,
        matches: MutableList<APIMeal>,
        onResponse: (List<APIMeal>) -> Unit
    ) {
        if (remainingIds.isEmpty()) {
            onResponse(matches)
            return
        }
        lookup(remainingIds.first()) { meal ->
            if (meal != null && meal.containsAllIngredients(selected)) {
                matches.add(meal)
            }
            collectAllMatches(remainingIds.drop(1), selected, matches, onResponse)
        }
    }

    private fun collectFilterCandidates(
        ingredients: List<String>,
        candidateIds: MutableSet<String>,
        index: Int,
        onComplete: (Set<String>) -> Unit
    ) {
        if (index >= ingredients.size) {
            onComplete(candidateIds)
            return
        }
        filterByIngredient(ingredients[index]) { meals ->
            meals.mapNotNull { it.idMeal }.forEach { candidateIds.add(it) }
            collectFilterCandidates(ingredients, candidateIds, index + 1, onComplete)
        }
    }

    private fun normalizeIngredient(name: String): String =
        name.trim().lowercase().replace(" ", "_")

    fun ingredientList(onResponse: (List<APIMeal>) -> Unit) {
        enqueue(mealAPI.ingredientList()) { response ->
            onResponse(response?.meals.orEmpty())
        }
    }
}
