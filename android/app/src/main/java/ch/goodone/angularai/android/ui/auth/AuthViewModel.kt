package ch.goodone.angularai.android.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.goodone.angularai.android.data.repository.AuthRepository
import ch.goodone.angularai.android.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = mutableStateOf(AuthUiState())
    val loginState: State<AuthUiState> = _loginState

    val currentUser = repository.currentUser

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.init()
        }
    }

    fun onLogin(login: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)
            val result = repository.login(login, pass)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.LoginSuccess(result.getOrThrow()))
            } else {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun onRegister(user: User, pass: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true)
            val result = repository.register(user, pass)
            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.RegisterSuccess)
            } else {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    data class AuthUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class UiEvent {
        data class LoginSuccess(val user: User) : UiEvent()
        object RegisterSuccess : UiEvent()
    }
}
