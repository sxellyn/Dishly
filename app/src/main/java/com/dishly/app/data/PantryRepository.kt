package com.dishly.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object PantryRepository {

    private const val USERS_COLLECTION = "users"
    private const val FIELD_PANTRY = "pantryIngredients"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /** In-memory fallback when Firestore is unavailable or denied. */
    private val localPantry: MutableSet<String> = linkedSetOf()

    suspend fun getIngredientNames(): Set<String> {
        val uid = auth.currentUser?.uid ?: return localPantry.toSet()
        return try {
            val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            val names = parseNameList(doc.get(FIELD_PANTRY)).toSet()
            localPantry.clear()
            localPantry.addAll(names)
            names
        } catch (_: Exception) {
            localPantry.toSet()
        }
    }

    suspend fun setIngredients(names: Set<String>) {
        val cleaned = names.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        localPantry.clear()
        localPantry.addAll(cleaned)
        persistLocal()
    }

    suspend fun addIngredient(name: String) {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return
        localPantry.add(cleaned)
        persistLocal()
    }

    suspend fun removeIngredient(name: String) {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return
        localPantry.removeAll { it.equals(cleaned, ignoreCase = true) }
        persistLocal()
    }

    private suspend fun persistLocal() {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(mapOf(FIELD_PANTRY to localPantry.toList()), SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Keep working from memory if rules/network fail.
        }
    }

    private fun parseNameList(value: Any?): List<String> =
        when (value) {
            is List<*> -> value.mapNotNull { item ->
                (item as? String)?.trim()?.takeIf { it.isNotBlank() }
            }
            else -> emptyList()
        }
}
