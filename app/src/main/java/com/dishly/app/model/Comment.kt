package com.dishly.app.model

data class Comment(
    val id: Int,
    val authorName: String,
    val text: String,
    val timeLabel: String = "now"
)
