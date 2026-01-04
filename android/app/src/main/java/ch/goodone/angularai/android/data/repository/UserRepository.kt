package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.remote.UserApi
import ch.goodone.angularai.android.data.remote.dto.UserDTO
import ch.goodone.angularai.android.domain.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getCurrentUser(): User = api.getCurrentUser().toDomain()

    suspend fun updateCurrentUser(user: User): User {
        return api.updateCurrentUser(user.toDto()).toDomain()
    }

    suspend fun getAllUsers(): List<User> {
        return api.getAllUsers().map { it.toDomain() }
    }

    suspend fun createUser(user: User, pass: String): User {
        return api.createUser(user.toDto().copy(password = pass)).toDomain()
    }

    suspend fun updateUser(user: User): User {
        return api.updateUser(user.id!!, user.toDto()).toDomain()
    }

    suspend fun deleteUser(id: Long) {
        api.deleteUser(id)
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

    private fun User.toDto() = UserDTO(
        id = id,
        firstName = firstName,
        lastName = lastName,
        login = login,
        email = email,
        birthDate = birthDate,
        address = address,
        role = role
    )
}
