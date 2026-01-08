package ch.goodone.angularai.android.data.remote.dto

data class TaskDTO(
    val id: Long? = null,
    val title: String,
    val description: String,
    val dueDate: String?,
    val priority: String,
    val status: String,
    val position: Int
)
