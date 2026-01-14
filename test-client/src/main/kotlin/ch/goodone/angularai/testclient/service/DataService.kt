package ch.goodone.angularai.testclient.service

import ch.goodone.angularai.testclient.model.Priority
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.TaskStatus
import ch.goodone.angularai.testclient.model.UserDTO
import ch.goodone.angularai.testclient.model.ActionLogDTO
import java.time.LocalDate
import java.time.LocalDateTime

object DataService {

    fun createSampleTasks(): List<TaskDTO> = listOf(
        TaskDTO(title = "Implement Dashboard", description = "Add a new dashboard menu to frontends", priority = Priority.HIGH, dueDate = LocalDate.now().plusDays(1), status = TaskStatus.IN_PROGRESS),
        TaskDTO(title = "Setup Project", description = "Initial project setup with Spring Boot", priority = Priority.HIGH, dueDate = LocalDate.now().minusDays(5), status = TaskStatus.COMPLETED),
        TaskDTO(title = "Implement Login", description = "Create login page and auth service", priority = Priority.MEDIUM, dueDate = LocalDate.now().minusDays(2), status = TaskStatus.COMPLETED),
        TaskDTO(title = "Write Unit Tests", description = "Ensure core logic is covered", priority = Priority.MEDIUM, dueDate = LocalDate.now().plusDays(3), status = TaskStatus.OPEN),
        TaskDTO(title = "Add Paging Support", description = "Generate 100+ tasks for paging test", priority = Priority.LOW, dueDate = LocalDate.now().plusWeeks(1), status = TaskStatus.OPEN),
        TaskDTO(title = "Fix CSS Issues", description = "Minor styling fixes for mobile", priority = Priority.LOW, dueDate = LocalDate.now().plusDays(10), status = TaskStatus.OPEN)
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

    fun createPagingLogs(count: Int): List<ActionLogDTO> {
        val users = listOf("admin", "testuser", "tester2", "jdoe")
        val actions = listOf("LOGIN", "TASK_CREATE", "TASK_UPDATE", "USER_UPDATE", "LOGOUT")
        val now = LocalDateTime.now()
        return (1..count).map { i ->
            ActionLogDTO(
                id = 0,
                timestamp = now.minusMinutes(i.toLong()),
                login = users[i % users.size],
                action = actions[i % actions.size],
                details = "Paging log entry #$i"
            )
        }
    }

    fun createSampleUsers(): List<UserDTO> = listOf(
        UserDTO(firstName = "Test", lastName = "User", login = "testuser", email = "test@example.com", password = "password123", role = "ROLE_USER"),
        UserDTO(firstName = "Another", lastName = "Tester", login = "tester2", email = "tester2@example.com", password = "password123", role = "ROLE_USER"),
        UserDTO(firstName = "Admin", lastName = "Support", login = "adminsupport", email = "support@example.com", password = "password123", role = "ROLE_ADMIN"),
        UserDTO(firstName = "Data", lastName = "Analyst", login = "analyst", email = "analyst@example.com", password = "password123", role = "ROLE_ADMIN_READ")
    )

    fun createSampleLogs(): List<ActionLogDTO> {
        val logs = mutableListOf<ActionLogDTO>()
        val now = LocalDateTime.now()
        val users = listOf("admin", "testuser", "tester2", "jdoe")
        val actions = listOf("LOGIN", "TASK_CREATE", "TASK_UPDATE", "USER_UPDATE", "LOGOUT")
        
        // Logs for 1 month
        for (i in 0 until 60) {
            val timestamp = now.minusHours(i * 12L) // Every 12 hours
            logs.add(
                ActionLogDTO(
                    id = 0,
                    timestamp = timestamp,
                    login = users[i % users.size],
                    action = actions[i % actions.size],
                    details = "Sample log entry ${60 - i}"
                )
            )
        }
        return logs
    }
}
