package com.dishly.app.data.auth

import android.content.Context
import android.net.Uri
import java.io.File

object ProfilePhotoStore {

    private const val PROFILE_DIR = "profile"

    fun photoFile(context: Context, uid: String): File {
        val dir = File(context.filesDir, PROFILE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$uid.jpg")
    }

    fun savePhoto(context: Context, uid: String, sourceUri: Uri): File {
        val file = photoFile(context, uid)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Could not read the selected image")
        return file
    }

    fun getPhotoPathIfExists(context: Context, uid: String): String? {
        val file = photoFile(context, uid)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }
}
