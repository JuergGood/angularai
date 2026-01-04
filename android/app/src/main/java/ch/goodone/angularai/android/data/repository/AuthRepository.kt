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
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val AUTH_KEY = stringPreferencesKey("auth_token")
    }

    val authToken: Flow<String?> = dataStore.data.map { it[AUTH_KEY] }

    suspend fun login(login: String, pass: String): Result<User> {
        return try {
            val token = Base64.encodeToString("$login:$pass".toByteArray(), Base64.NO_WRAP)
            val response = api.login("Basic $token")
            if (response.isSuccessful) {
                val userDto = response.body()!!
                dataStore.edit { it[AUTH_KEY] = token }
                Result.success(userDto.toDomain())
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
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
                Result.failure(Exception("Registration failed: ${response.code()}"))
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
