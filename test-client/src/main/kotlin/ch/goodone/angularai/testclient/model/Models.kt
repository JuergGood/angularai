package ch.goodone.angularai.testclient.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

enum class Priority { LOW, MEDIUM, HIGH }
enum class TaskStatus { OPEN, IN_PROGRESS, COMPLETED, CLOSED }

data class TaskDTO(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dueDate: LocalDate? = null,
    val priority: Priority? = Priority.MEDIUM,
    val status: TaskStatus? = TaskStatus.OPEN,
    val position: Int? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: java.time.LocalDateTime? = null
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
    val role: String? = "ROLE_USER",
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: java.time.LocalDateTime? = null
)

data class ActionLogDTO(
    val id: Long? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
