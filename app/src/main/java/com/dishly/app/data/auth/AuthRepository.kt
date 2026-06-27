package com.dishly.app.data.auth

import android.content.Context
import android.net.Uri
import com.dishly.app.data.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private const val USERNAMES_COLLECTION = "usernames"
    private const val USERS_COLLECTION = "users"
    private const val FIELD_EMAIL = "email"
    private const val FIELD_UID = "uid"
    private const val FIELD_USERNAME = "username"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() {
        auth.signOut()
    }

    suspend fun signIn(context: Context, username: String, password: String): Result<Unit> = runCatching {
        val email = resolveEmailForUsername(username)
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Sign in failed")
        syncLocalUserFromFirebaseUser(context, user.uid, user.displayName, user.email)
    }

    suspend fun signUp(username: String, email: String, password: String): Result<Unit> = runCatching {
        val normalizedUsername = normalizeUsername(username)
        val normalizedEmail = normalizeEmail(email)

        if (isUsernameTaken(normalizedUsername)) {
            throw UsernameTakenException()
        }

        val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
        val user = result.user ?: throw IllegalStateException("Sign up failed")
        val uid = user.uid

        try {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(username.trim())
                    .build()
            ).await()

            val usernameRef = firestore.collection(USERNAMES_COLLECTION).document(normalizedUsername)
            val userRef = firestore.collection(USERS_COLLECTION).document(uid)
            val batch = firestore.batch()
            batch.set(
                usernameRef,
                mapOf(
                    FIELD_EMAIL to normalizedEmail,
                    FIELD_UID to uid
                )
            )
            batch.set(
                userRef,
                mapOf(
                    FIELD_USERNAME to normalizedUsername,
                    FIELD_EMAIL to normalizedEmail
                )
            )
            batch.commit().await()
        } catch (e: Exception) {
            user.delete().await()
            throw e
        }

        syncLocalUser(username.trim(), normalizedUsername, null)
    }

    suspend fun sendPasswordReset(username: String): Result<Unit> = runCatching {
        val email = resolveEmailForUsername(username)
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun updateProfile(
        context: Context,
        name: String,
        newPhotoUri: Uri? = null
    ): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user logged in")
        val trimmedName = name.trim()

        if (newPhotoUri != null) {
            ProfilePhotoStore.savePhoto(context, user.uid, newPhotoUri)
        }

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
        ).await()

        val photoPath = ProfilePhotoStore.getPhotoPathIfExists(context, user.uid)
        RecipeRepository.updateUser(trimmedName, RecipeRepository.currentUser.username, photoPath)
    }

    suspend fun syncLocalUserFromSession(context: Context) {
        val user = auth.currentUser
        if (user != null) {
            syncLocalUserFromFirebaseUser(context, user.uid, user.displayName, user.email)
        }
    }

    private suspend fun syncLocalUserFromFirebaseUser(
        context: Context?,
        uid: String,
        displayName: String?,
        email: String?
    ) {
        val userDoc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        val username = if (userDoc.exists()) {
            userDoc.getString(FIELD_USERNAME).orEmpty()
        } else {
            legacyUsernameFromEmail(email)
        }
        val photoPath = context?.let { ProfilePhotoStore.getPhotoPathIfExists(it, uid) }
        val name = displayName?.takeIf { it.isNotBlank() } ?: username
        syncLocalUser(name, username, photoPath)
    }

    private fun syncLocalUser(name: String, username: String, photoPath: String?) {
        RecipeRepository.updateUser(name = name, username = username, photoUrl = photoPath)
    }

    private suspend fun resolveEmailForUsername(username: String): String {
        val normalizedUsername = normalizeUsername(username)
        val doc = firestore.collection(USERNAMES_COLLECTION)
            .document(normalizedUsername)
            .get()
            .await()
        if (doc.exists()) {
            return doc.getString(FIELD_EMAIL)
                ?: throw IllegalStateException("Account not found")
        }
        return legacyEmailFromUsername(normalizedUsername)
    }

    private suspend fun isUsernameTaken(normalizedUsername: String): Boolean {
        val doc = firestore.collection(USERNAMES_COLLECTION)
            .document(normalizedUsername)
            .get()
            .await()
        return doc.exists()
    }

    fun mapAuthError(error: Throwable): String {
        when (error) {
            is UsernameTakenException -> return "Username already in use"
        }
        if (error is FirebaseFirestoreException) {
            return when (error.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Firestore permission denied. Update security rules in Firebase Console."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Could not reach Firestore. Check your internet connection."
                else -> error.message ?: "Firestore error"
            }
        }
        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_USER_NOT_FOUND" -> "Account not found"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection"
                else -> error.localizedMessage ?: "Authentication failed"
            }
        }
        return error.localizedMessage ?: "Authentication failed"
    }

    private fun normalizeUsername(username: String): String =
        username.trim().lowercase().replace(" ", "_")

    private fun normalizeEmail(email: String): String =
        email.trim().lowercase()

    private fun legacyEmailFromUsername(normalizedUsername: String): String =
        "$normalizedUsername@dishly.app"

    private fun legacyUsernameFromEmail(email: String?): String =
        email?.substringBefore("@").orEmpty()

    private class UsernameTakenException : Exception()
}
