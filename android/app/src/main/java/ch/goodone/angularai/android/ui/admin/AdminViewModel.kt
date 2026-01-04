package ch.goodone.angularai.android.ui.admin

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
class AdminViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminUiState())
    val state: State<AdminUiState> = _state

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val users = repository.getAllUsers()
                _state.value = _state.value.copy(users = users, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun onDeleteUser(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteUser(id)
                loadUsers()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun onSaveUser(user: User, pass: String?) {
        viewModelScope.launch {
            try {
                if (user.id == null) {
                    repository.createUser(user, pass ?: "password123")
                } else {
                    repository.updateUser(user)
                }
                loadUsers()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun getUser(id: Long): User? {
        return _state.value.users.find { it.id == id }
    }

    data class AdminUiState(
        val users: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
