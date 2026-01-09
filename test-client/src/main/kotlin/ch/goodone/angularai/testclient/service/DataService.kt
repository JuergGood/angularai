package ch.goodone.angularai.testclient.service

import ch.goodone.angularai.testclient.model.Priority
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.TaskStatus
import ch.goodone.angularai.testclient.model.UserDTO
import java.time.LocalDate

object DataService {

    fun createSampleTasks(): List<TaskDTO> = listOf(
        TaskDTO(title = "Implement TestClient", description = "Create the CLI tool for data management", priority = Priority.HIGH, dueDate = LocalDate.now().plusDays(1)),
        TaskDTO(title = "Write Unit Tests", description = "Ensure core logic is covered", priority = Priority.MEDIUM, dueDate = LocalDate.now().plusDays(3)),
        TaskDTO(title = "Add Paging Support", description = "Generate 100+ tasks for paging test", priority = Priority.LOW, dueDate = LocalDate.now().plusWeeks(1))
    )

    fun createPagingTasks(count: Int): List<TaskDTO> {
        return (1..count).map { i ->
            TaskDTO(
                title = "Paging Task #$i",
                description = "Automated task for paging verification #$i",
                priority = Priority.values()[i % 3],
                status = TaskStatus.values()[i % 3],
                dueDate = LocalDate.now().plusDays(i.toLong() % 30)
            )
        }
    }

    fun createSampleUsers(): List<UserDTO> = listOf(
        UserDTO(firstName = "Test", lastName = "User", login = "testuser", email = "test@example.com", password = "password123", role = "ROLE_USER"),
        UserDTO(firstName = "Another", lastName = "Tester", login = "tester2", email = "tester2@example.com", password = "password123", role = "ROLE_USER")
    )
}
