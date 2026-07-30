package com.dishly.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FavoritesRepository {

    private const val USERS_COLLECTION = "users"
    private const val FAVORITES_COLLECTION = "favorites"
    private const val FIELD_RECIPE_ID = "recipeId"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getFavoriteIds(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .get()
            .await()
        return snapshot.documents.map { doc ->
            doc.getString(FIELD_RECIPE_ID) ?: doc.id
        }
    }

    suspend fun isFavorite(recipeId: Int): Boolean {
        if (recipeId <= 0) return false
        val uid = auth.currentUser?.uid ?: return false
        val doc = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .document(recipeId.toString())
            .get()
            .await()
        return doc.exists()
    }

    suspend fun addFavorite(recipeId: Int) {
        if (recipeId <= 0) return
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .document(recipeId.toString())
            .set(mapOf(FIELD_RECIPE_ID to recipeId.toString()))
            .await()
    }

    suspend fun removeFavorite(recipeId: Int) {
        if (recipeId <= 0) return
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(FAVORITES_COLLECTION)
            .document(recipeId.toString())
            .delete()
            .await()
    }
}
