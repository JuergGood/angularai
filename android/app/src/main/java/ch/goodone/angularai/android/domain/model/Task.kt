package ch.goodone.angularai.android.domain.model

data class Task(
    val id: Long? = null,
    val title: String,
    val description: String,
    val dueDate: String?,
    val priority: String,
    val status: TaskStatus = TaskStatus.OPEN,
    val position: Int = 0
)
