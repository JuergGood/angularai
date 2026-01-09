package ch.goodone.angularai.testclient.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

enum class Priority { LOW, MEDIUM, HIGH }
enum class TaskStatus { OPEN, IN_PROGRESS, COMPLETED }

data class TaskDTO(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dueDate: LocalDate? = null,
    val priority: Priority? = Priority.MEDIUM,
    val status: TaskStatus? = TaskStatus.OPEN,
    val position: Int? = null
)

data class UserDTO(
    val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val login: String,
    val password: String? = null,
    val email: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,
    val address: String? = null,
    val role: String? = "ROLE_USER"
)

data class ActionLogDTO(
    val id: Long? = null,
    val timestamp: LocalDateTime? = null,
    val login: String,
    val action: String,
    val details: String? = null
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
)
