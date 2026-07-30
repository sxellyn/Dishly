package com.dishly.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object RecentRecipesRepository {

    private const val USERS_COLLECTION = "users"
    private const val FIELD_RECENT_RECIPE_IDS = "recentRecipeIds"
    private const val MAX_RECENT = 4

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getRecentRecipeIds(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        if (!doc.exists()) return emptyList()
        return parseIdList(doc.get(FIELD_RECENT_RECIPE_IDS)).take(MAX_RECENT)
    }

    suspend fun addRecentRecipe(recipeId: Int) {
        if (recipeId <= 0) return
        val uid = auth.currentUser?.uid ?: return
        val idStr = recipeId.toString()
        val docRef = firestore.collection(USERS_COLLECTION).document(uid)
        val doc = docRef.get().await()
        val current = if (doc.exists()) {
            parseIdList(doc.get(FIELD_RECENT_RECIPE_IDS))
        } else {
            emptyList()
        }
        val updated = (listOf(idStr) + current.filter { it != idStr }).take(MAX_RECENT)
        docRef.set(
            mapOf(FIELD_RECENT_RECIPE_IDS to updated),
            SetOptions.merge()
        ).await()
    }

    private fun parseIdList(value: Any?): List<String> =
        when (value) {
            is List<*> -> value.mapNotNull { item ->
                when (item) {
                    is String -> item
                    is Number -> item.toString()
                    else -> null
                }
            }
            else -> emptyList()
        }
}
