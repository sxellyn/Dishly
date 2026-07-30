package com.dishly.app.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object RecipeStatsRepository {

    private const val RECIPES_COLLECTION = "recipes"
    private const val FIELD_FAVORITE_COUNT = "favoriteCount"

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getFavoriteCount(recipeId: Int): Int {
        if (recipeId <= 0) return 0
        val doc = firestore.collection(RECIPES_COLLECTION)
            .document(recipeId.toString())
            .get()
            .await()
        if (!doc.exists()) return 0
        return doc.getLong(FIELD_FAVORITE_COUNT)?.toInt() ?: 0
    }

    suspend fun getFavoriteCounts(recipeIds: List<Int>): Map<Int, Int> {
        if (recipeIds.isEmpty()) return emptyMap()
        return recipeIds.associateWith { getFavoriteCount(it) }
    }

    suspend fun incrementFavoriteCount(recipeId: Int) {
        if (recipeId <= 0) return
        firestore.collection(RECIPES_COLLECTION)
            .document(recipeId.toString())
            .set(
                mapOf(FIELD_FAVORITE_COUNT to FieldValue.increment(1)),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun decrementFavoriteCount(recipeId: Int) {
        if (recipeId <= 0) return
        firestore.collection(RECIPES_COLLECTION)
            .document(recipeId.toString())
            .set(
                mapOf(FIELD_FAVORITE_COUNT to FieldValue.increment(-1)),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun getTopFavoritedRecipes(limit: Int = 4): List<Pair<Int, Int>> {
        val snapshot = firestore.collection(RECIPES_COLLECTION)
            .whereGreaterThan(FIELD_FAVORITE_COUNT, 0)
            .orderBy(FIELD_FAVORITE_COUNT, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id.toIntOrNull() ?: return@mapNotNull null
            val count = doc.getLong(FIELD_FAVORITE_COUNT)?.toInt() ?: 0
            id to count
        }
    }
}
