package com.dishly.app.model

/**
 * Model (MVVM domain) — the user logged into the app.
 *
 * Kept in memory to simulate the profile without real authentication.
 * Sign up only collects a username and password, so the profile holds
 * just the display name and the username.
 */
data class User(
    var name: String,
    var username: String
)
