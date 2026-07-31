package com.dishly.app.data

import com.dishly.app.model.ShoppingList
import com.dishly.app.model.ShoppingListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object ShoppingListRepository {

    private const val USERS_COLLECTION = "users"
    private const val FIELD_SHOPPING_LIST = "activeShoppingList"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var localList: ShoppingList? = null

    suspend fun getActiveList(): ShoppingList? {
        val uid = auth.currentUser?.uid
        if (uid == null) return localList

        return try {
            val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            @Suppress("UNCHECKED_CAST")
            val raw = doc.get(FIELD_SHOPPING_LIST) as? Map<String, Any?> ?: return localList
            val parsed = parseList(raw) ?: return localList
            localList = parsed
            parsed
        } catch (_: Exception) {
            localList
        }
    }

    suspend fun saveActiveList(list: ShoppingList) {
        localList = list
        val uid = auth.currentUser?.uid ?: return
        val payload = mapOf(
            FIELD_SHOPPING_LIST to mapOf(
                "recipeId" to list.recipeId,
                "recipeTitle" to list.recipeTitle,
                "items" to list.items.map { item ->
                    mapOf(
                        "id" to item.id,
                        "name" to item.name,
                        "measure" to item.measure,
                        "checked" to item.checked,
                        "recipeId" to (item.recipeId ?: list.recipeId)
                    )
                }
            )
        )
        try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(payload, SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Keep working from memory if rules/network fail.
        }
    }

    suspend fun toggleChecked(itemId: String, checked: Boolean) {
        val current = localList ?: getActiveList() ?: return
        val updated = current.copy(
            items = current.items.map { item ->
                if (item.id == itemId) item.copy(checked = checked) else item
            }
        )
        saveActiveList(updated)
    }

    suspend fun clearActiveList() {
        localList = null
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(mapOf(FIELD_SHOPPING_LIST to null), SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Ignore persistence failures.
        }
    }

    private fun parseList(raw: Map<String, Any?>): ShoppingList? {
        val recipeId = (raw["recipeId"] as? Number)?.toInt() ?: return null
        val title = raw["recipeTitle"] as? String ?: ""
        @Suppress("UNCHECKED_CAST")
        val rawItems = raw["items"] as? List<Map<String, Any?>> ?: emptyList()
        val items = rawItems.mapNotNull { map ->
            val name = map["name"] as? String ?: return@mapNotNull null
            ShoppingListItem(
                id = map["id"] as? String ?: name,
                name = name,
                measure = map["measure"] as? String ?: "",
                checked = map["checked"] as? Boolean ?: false,
                recipeId = (map["recipeId"] as? Number)?.toInt() ?: recipeId
            )
        }
        return ShoppingList(recipeId = recipeId, recipeTitle = title, items = items)
    }
}
