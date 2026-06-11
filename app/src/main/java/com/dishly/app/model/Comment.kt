package com.dishly.app.model

/**
 * Model (MVVM domain) — a comment left by a user on a recipe.
 *
 * Comments are kept in memory only (no backend), matching the scope
 * of this delivery.
 */
data class Comment(
    val id: Int,
    val authorName: String,
    val text: String,
    val timeLabel: String = "now"
)
