package com.dishly.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dishly.app.data.RecipeRepository
import com.dishly.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val pendingPhotoUri: Uri? = null,
    val saved: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            AuthRepository.syncLocalUserFromSession(getApplication())
            val user = RecipeRepository.currentUser
            _uiState.value = EditProfileUiState(
                name = user.name,
                username = user.username,
                photoUrl = user.photoUrl
            )
        }
    }

    fun onNameChange(v: String) {
        _uiState.value = _uiState.value.copy(name = v, error = null)
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(pendingPhotoUri = uri, error = null)
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Please enter your name")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = AuthRepository.updateProfile(
                context = getApplication(),
                name = state.name,
                newPhotoUri = state.pendingPhotoUri
            )
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, saved = true)
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = AuthRepository.mapAuthError(result.exceptionOrNull()!!)
                )
            }
        }
    }
}
