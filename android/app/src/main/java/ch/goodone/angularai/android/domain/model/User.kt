package ch.goodone.angularai.android.domain.model

data class User(
    val id: Long? = null,
    val firstName: String = "",
    val lastName: String = "",
    val login: String = "",
    val email: String = "",
    val birthDate: String = "",
    val address: String = "",
    val role: String = ""
)
