package ch.goodone.angularai.android.data.remote.dto

data class UserDTO(
    val id: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val login: String? = null,
    val email: String? = null,
    val birthDate: String? = null, // yyyy-MM-dd
    val address: String? = null,
    val role: String? = null,
    val password: String? = null
)
