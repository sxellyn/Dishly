package com.dishly.app.viewmodel

import androidx.lifecycle.ViewModel
import com.dishly.app.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EditProfileUiState(
    val name: String = "",
    val username: String = "",
    val saved: Boolean = false
)

class EditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun load() {
        val user = RecipeRepository.currentUser
        _uiState.value = EditProfileUiState(
            name = user.name,
            username = user.username
        )
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v) }

    fun save() {
        val state = _uiState.value
        RecipeRepository.updateUser(state.name, state.username)
        _uiState.value = state.copy(saved = true)
    }
}
