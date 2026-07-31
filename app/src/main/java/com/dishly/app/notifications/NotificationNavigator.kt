package com.dishly.app.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bridges notification taps into Compose navigation.
 */
object NotificationNavigator {
    @Volatile
    var pendingRecipeId: Int? = null
        private set

    private val _events = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val events: SharedFlow<Int> = _events.asSharedFlow()

    fun openRecipe(recipeId: Int) {
        if (recipeId <= 0) return
        pendingRecipeId = recipeId
        _events.tryEmit(recipeId)
    }

    fun consumePendingRecipeId(): Int? {
        val id = pendingRecipeId
        pendingRecipeId = null
        return id
    }
}
