package ch.goodone.angularai.android.data.remote.dto

data class ActionLogDTO(
    val id: Long,
    val timestamp: String,
    val login: String,
    val action: String,
    val details: String
)

data class LogResponseDTO(
    val content: List<ActionLogDTO>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
)
