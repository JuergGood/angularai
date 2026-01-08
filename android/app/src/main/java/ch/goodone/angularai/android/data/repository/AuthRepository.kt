package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.remote.AuthApi
import ch.goodone.angularai.android.data.remote.dto.UserDTO
import ch.goodone.angularai.android.domain.model.User
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val userRepository: UserRepository,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val AUTH_KEY = stringPreferencesKey("auth_token")
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val authToken: Flow<String?> = dataStore.data.map { it[AUTH_KEY] }

    suspend fun init() {
        authToken.firstOrNull()?.let {
            try {
                _currentUser.value = userRepository.getCurrentUser()
            } catch (e: Exception) {
                logout()
            }
        }
    }

    suspend fun login(login: String, pass: String): Result<User> {
        return try {
            val token = Base64.encodeToString("$login:$pass".toByteArray(), Base64.NO_WRAP)
            val response = api.login("Basic $token")
            if (response.isSuccessful) {
                val userDto = response.body()!!
                dataStore.edit { it[AUTH_KEY] = token }
                val user = userDto.toDomain()
                _currentUser.value = user
                Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Login failed: ${response.code()}"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: User, pass: String): Result<User> {
        return try {
            val dto = UserDTO(
                firstName = user.firstName,
                lastName = user.lastName,
                login = user.login,
                email = user.email,
                birthDate = user.birthDate,
                address = user.address,
                password = pass
            )
            val response = api.register(dto)
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Registration failed: ${response.code()}"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } finally {
            dataStore.edit { it.remove(AUTH_KEY) }
            _currentUser.value = null
        }
    }

    private fun UserDTO.toDomain() = User(
        id = id,
        firstName = firstName ?: "",
        lastName = lastName ?: "",
        login = login ?: "",
        email = email ?: "",
        birthDate = birthDate ?: "",
        address = address ?: "",
        role = role ?: ""
    )
}
