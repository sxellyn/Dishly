package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import com.dishly.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(val user: User = RecipeRepository.currentUser)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = ProfileUiState(RecipeRepository.currentUser)
    }
}
