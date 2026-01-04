package ch.goodone.angularai.android.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.repository.UserRepository
import ch.goodone.angularai.android.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileUiState())
    val state: State<ProfileUiState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = repository.getCurrentUser()
                _state.value = _state.value.copy(user = user, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun onUpdateProfile(user: User) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val updatedUser = repository.updateCurrentUser(user)
                _state.value = _state.value.copy(user = updatedUser, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    data class ProfileUiState(
        val user: User? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
