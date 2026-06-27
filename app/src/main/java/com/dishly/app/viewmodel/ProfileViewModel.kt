package com.dishly.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dishly.app.data.RecipeRepository
import com.dishly.app.data.auth.AuthRepository
import com.dishly.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(val user: User = RecipeRepository.currentUser)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            AuthRepository.syncLocalUserFromSession(getApplication())
            _uiState.value = ProfileUiState(RecipeRepository.currentUser)
        }
    }
}
